package com.splitter.controller;

import com.splitter.dto.*;
import com.splitter.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<GroupResponse> create(@Valid @RequestBody GroupRequest request) {
        return ResponseEntity.ok(groupService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<GroupResponse>> myGroups() {
        return ResponseEntity.ok(groupService.listMyGroups());
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<GroupResponse> getOne(@PathVariable Long groupId) {
        return ResponseEntity.ok(groupService.getById(groupId));
    }

    @PostMapping("/{groupId}/members")
    public ResponseEntity<GroupResponse> addMember(@PathVariable Long groupId, @Valid @RequestBody AddMemberRequest request) {
        return ResponseEntity.ok(groupService.addMember(groupId, request));
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<Void> removeMember(@PathVariable Long groupId, @PathVariable Long userId) {
        groupService.removeMember(groupId, userId);
        return ResponseEntity.noContent().build();
    }

       @PostMapping("/{groupId}/invite")
    public ResponseEntity<InviteResponse> generateInvite(@PathVariable Long groupId) {
        return ResponseEntity.ok(groupService.generateInvite(groupId));
    }
 
    @GetMapping("/{groupId}/invite")
    public ResponseEntity<InviteResponse> getInvite(@PathVariable Long groupId) {
        InviteResponse invite = groupService.getInvite(groupId);
        return invite == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(invite);
    }
 
    @DeleteMapping("/{groupId}/invite")
    public ResponseEntity<Void> revokeInvite(@PathVariable Long groupId) {
        groupService.revokeInvite(groupId);
        return ResponseEntity.noContent().build();
    }
 
    /** Not nested under /groups/{groupId} since the caller doesn't know the group
     *  ID yet — the token itself is what identifies which group to join. */
    @PostMapping("/join/{token}")
    public ResponseEntity<GroupResponse> join(@PathVariable String token) {
        return ResponseEntity.ok(groupService.joinViaInvite(token));
    }
}
