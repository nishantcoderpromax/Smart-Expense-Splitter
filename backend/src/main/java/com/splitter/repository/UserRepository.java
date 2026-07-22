package com.splitter.repository;
import com.splitter.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE lower(u.name) LIKE lower(concat('%', :query, '%')) " +
            "OR lower(u.email) LIKE lower(concat('%', :query, '%'))")
    List<User> searchByNameOrEmail(@Param("query") String query, Pageable pageable);
}
