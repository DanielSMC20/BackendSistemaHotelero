package Hotel.jwt.repository;
import Hotel.jwt.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenAndExpiresAtAfterAndUsedAtIsNull(String token, LocalDateTime now);
    void deleteByEmail(String email);
    void deleteByExpiresAtBefore(LocalDateTime t);

}