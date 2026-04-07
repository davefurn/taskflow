package com.taskflow.api.repository.authAndUsers;

import com.taskflow.api.entity.PasswordResetToken;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    // POST /api/auth/verify-email
    Optional<PasswordResetToken> findByTokenAndType(String token, PasswordResetToken.TokenType type);

    // POST /api/auth/reset-password
    Optional<PasswordResetToken> findByTokenAndTypeAndExpiresAtAfter(
            String token,
            PasswordResetToken.TokenType type,
            Instant now
    );

    // Cleanup — delete all tokens for a user after use
    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetToken t WHERE t.user.id = :userId AND t.type = :type")
    void deleteAllByUserIdAndType(UUID userId, PasswordResetToken.TokenType type);

    // Cleanup expired tokens (called by scheduled job)
    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetToken t WHERE t.expiresAt < :now")
    void deleteAllExpired(Instant now);
}