package br.com.aisaas.auth.infra;

import br.com.aisaas.auth.domain.PasswordResetToken;
import br.com.aisaas.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long>{
    Optional<PasswordResetToken> findFirstByUserIdOrderByCreatedAtDesc(long id);

    List<PasswordResetToken> findByUserIdAndUsedAtIsNull(long userId);
}
