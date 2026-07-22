package com.splitter.repository;

import com.splitter.entity.VerificationToken;
import com.splitter.enums.TokenPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByTokenAndPurpose(String token, TokenPurpose purpose);
    void deleteByUserIdAndPurpose(Long userId, TokenPurpose purpose);
}
