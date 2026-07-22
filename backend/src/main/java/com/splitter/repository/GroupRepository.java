package com.splitter.repository;

import com.splitter.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long> {

    @Query("SELECT gm.group FROM GroupMember gm WHERE gm.user.id = :userId")
    List<Group> findAllForUser(@Param("userId") Long userId);

    Optional<Group> findByInviteToken(String inviteToken);
}

