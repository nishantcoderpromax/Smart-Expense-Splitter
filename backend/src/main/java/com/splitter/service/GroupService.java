package com.splitter.service;

import com.splitter.dto.*;
import com.splitter.entity.Group;
import com.splitter.entity.GroupMember;
import com.splitter.entity.User;
import com.splitter.enums.ActivityType;
import com.splitter.enums.GroupRole;
import com.splitter.exception.ApiException;
import com.splitter.repository.GroupMemberRepository;
import com.splitter.repository.GroupRepository;
import com.splitter.repository.UserRepository;
import com.splitter.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
 
import java.util.List;
import java.time.LocalDateTime;
import java.util.UUID;
 
@Service
@RequiredArgsConstructor
public class GroupService {
 
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final GroupEventPublisher eventPublisher;
    private final ActivityLogService activityLogService;
 
    @Value("${app.frontend-url}")
    private String frontendUrl;
 
    public GroupResponse create(GroupRequest request) {
        User creator = currentUser();
 
        Group group = Group.builder()
                .name(request.getName())
                .description(request.getDescription())
                .createdBy(creator)
                .build();
        groupRepository.save(group);
 
        GroupMember owner = GroupMember.builder().group(group).user(creator).role(GroupRole.ADMIN).build();
        groupMemberRepository.save(owner);
 
        return toResponse(group);
    }
 
    public List<GroupResponse> listMyGroups() {
        return groupRepository.findAllForUser(currentUser().getId())
                .stream().map(this::toResponse).toList();
    }
 
    public GroupResponse getById(Long groupId) {
        return toResponse(findGroupOrThrow(groupId));
    }
 
    public GroupResponse addMember(Long groupId, AddMemberRequest request) {
        Group group = findGroupOrThrow(groupId);
        requireAdmin(group, currentUser());
 
        User newMember = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No user with that email"));
 
        if (groupMemberRepository.existsByGroupIdAndUserId(groupId, newMember.getId())) {
            throw new ApiException(HttpStatus.CONFLICT, "User is already a member");
        }
 
        groupMemberRepository.save(GroupMember.builder().group(group).user(newMember).role(GroupRole.MEMBER).build());
 
        User actor = currentUser();
        activityLogService.record(group, actor, ActivityType.MEMBER_ADDED,
                String.format("%s added %s to the group", actor.getName(), newMember.getName()));
        eventPublisher.publish(groupId, "MEMBER_ADDED");
 
        return toResponse(findGroupOrThrow(groupId));
    }
 
    public void removeMember(Long groupId, Long userId) {
        Group group = findGroupOrThrow(groupId);
        User actor = currentUser();
        requireAdmin(group, actor);
 
        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Member not found"));
        String removedName = member.getUser().getName();
        groupMemberRepository.delete(member);
 
        activityLogService.record(group, actor, ActivityType.MEMBER_REMOVED,
                String.format("%s removed %s from the group", actor.getName(), removedName));
        eventPublisher.publish(groupId, "MEMBER_REMOVED");
    }
 
    // --- invite links ---
 
    /** Admin-only: (re)generates the shareable link, invalidating any previous one. */
    public InviteResponse generateInvite(Long groupId) {
        Group group = findGroupOrThrow(groupId);
        requireAdmin(group, currentUser());
 
        group.setInviteToken(UUID.randomUUID().toString());
        group.setInviteExpiresAt(LocalDateTime.now().plusDays(7));
        groupRepository.save(group);
 
        return toInviteResponse(group);
    }
 
    /** Any member can view/share the current link, but not create one. */
    public InviteResponse getInvite(Long groupId) {
        Group group = findGroupOrThrow(groupId);
        requireMember(groupId, currentUser().getId());
 
        if (group.getInviteToken() == null) {
            return null; // no active invite — frontend shows a "Generate Link" prompt instead
        }
        return toInviteResponse(group);
    }
 
    public void revokeInvite(Long groupId) {
        Group group = findGroupOrThrow(groupId);
        requireAdmin(group, currentUser());
 
        group.setInviteToken(null);
        group.setInviteExpiresAt(null);
        groupRepository.save(group);
    }
 
    /** Anyone with a valid, unexpired link can join — no invitation from an existing member needed. */
    public GroupResponse joinViaInvite(String token) {
        Group group = groupRepository.findByInviteToken(token)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "This invite link is invalid"));
 
        if (group.getInviteExpiresAt() == null || group.getInviteExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ApiException(HttpStatus.GONE, "This invite link has expired");
        }
 
        User user = currentUser();
 
        if (groupMemberRepository.existsByGroupIdAndUserId(group.getId(), user.getId())) {
            return toResponse(group); // already a member — just take them to the group, no error
        }
 
        groupMemberRepository.save(GroupMember.builder().group(group).user(user).role(GroupRole.MEMBER).build());
 
        activityLogService.record(group, user, ActivityType.MEMBER_ADDED,
                String.format("%s joined via invite link", user.getName()));
        eventPublisher.publish(group.getId(), "MEMBER_ADDED");
 
        return toResponse(findGroupOrThrow(group.getId()));
    }
 
    // --- helpers ---
 
    private void requireMember(Long groupId, Long userId) {
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You are not a member of this group");
        }
    }
 
    private InviteResponse toInviteResponse(Group group) {
        String url = frontendUrl + "/join/" + group.getInviteToken();
        return new InviteResponse(group.getInviteToken(), url, group.getInviteExpiresAt());
    }
 
    private User currentUser() {
        return userRepository.findByEmail(CurrentUser.email())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));
    }
 
    private Group findGroupOrThrow(Long id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Group not found"));
    }
 
    private void requireAdmin(Group group, User user) {
        boolean isAdmin = groupMemberRepository.findByGroupIdAndUserId(group.getId(), user.getId())
                .map(m -> m.getRole() == GroupRole.ADMIN)
                .orElse(false);
        if (!isAdmin) throw new ApiException(HttpStatus.FORBIDDEN, "Only a group admin can do this");
    }
 
    private GroupResponse toResponse(Group group) {
        List<MemberResponse> members = group.getMembers().stream()
                .map(m -> new MemberResponse(m.getUser().getId(), m.getUser().getName(), m.getUser().getEmail(), m.getRole().name()))
                .toList();
        return new GroupResponse(group.getId(), group.getName(), group.getDescription(), group.getCreatedBy().getName(), members);
    }
}