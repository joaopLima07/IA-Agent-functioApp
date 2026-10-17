package br.com.aisaas.auth.application;

import br.com.aisaas.auth.api.dto.ForgotPasswordRequest;
import br.com.aisaas.auth.api.dto.ResetPasswordRequest;
import br.com.aisaas.auth.api.dto.VerifyResetCodeRequest;
import br.com.aisaas.auth.api.dto.VerifyResetCodeResponse;
import br.com.aisaas.auth.domain.PasswordResetToken;
import br.com.aisaas.auth.domain.User;
import br.com.aisaas.auth.domain.exceptions.InvalidResetTokenException;
import br.com.aisaas.auth.domain.exceptions.PasswordSimilarToOldOneException;
import br.com.aisaas.auth.infra.PasswordResetTokenRepository;
import br.com.aisaas.auth.infra.UserRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
public class PasswordResetService {
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final Sanitizer sanitizer;
    private final SendEmail sendEmail;
    private final ForgotPasswordTokenPolicy forgotPasswordTokenPolicy;
    private final UserRepository userRepository;
    private final PasswordPolicy passwordPolicy;

    int expiresAtMin = 10;
    private static final int MAX_ATTEMPTS = 5;

    public PasswordResetService(PasswordResetTokenRepository passwordResetTokenRepository, Sanitizer sanitizer, SendEmail sendEmail, ForgotPasswordTokenPolicy forgotPasswordTokenPolicy, UserRepository userRepository, PasswordPolicy passwordPolicy) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.sanitizer = sanitizer;
        this.sendEmail = sendEmail;
        this.forgotPasswordTokenPolicy = forgotPasswordTokenPolicy;
        this.userRepository = userRepository;
        this.passwordPolicy = passwordPolicy;
    }

    Optional<User> findByEmail(String email){
        return userRepository.findByEmail(email);
    }

    public void generatedToken(ForgotPasswordRequest forgotPasswordRequest){
        String emailSanitizer = sanitizer.sanitizeEmail(forgotPasswordRequest.email());

        Optional<User> user = findByEmail(emailSanitizer);

        if (user.isEmpty()) {
            return;
        }

        // Only the newest code is valid: retire every earlier one that is still open.
        LocalDateTime now = LocalDateTime.now();
        List<PasswordResetToken> open = passwordResetTokenRepository.findByUserIdAndUsedAtIsNull(user.get().getId());
        for (PasswordResetToken old : open) {
            old.setUsedAt(now);
            old.setResetTokenHash(null);
            old.setResetTokenExpiresAt(null);
        }
        passwordResetTokenRepository.saveAll(open);

        SecureRandom random = new SecureRandom();

        String token = String.format("%06d", random.nextInt(1_000_000));

        String hashToken = forgotPasswordTokenPolicy.hashToken(token);

        PasswordResetToken passwordResetToken = new PasswordResetToken();

        passwordResetToken.setHashToken(hashToken);
        passwordResetToken.setUserId(user.get().getId());

        passwordResetToken.setExpiresAt(now.plusMinutes(expiresAtMin));

        passwordResetTokenRepository.save(passwordResetToken);

        sendEmail.sendEmail(emailSanitizer, token, user.get().getFullName(), expiresAtMin);
    }


    Optional<PasswordResetToken> findToken(long userId){
        return Optional.of(passwordResetTokenRepository.findFirstByUserIdOrderByCreatedAtDesc(userId).orElseThrow(() -> new InvalidResetTokenException("Este token não é válido.")));
    }

    private PasswordResetToken checkCode(User user, String code){
        PasswordResetToken userToken = findToken(user.getId()).get();

        if (userToken.getUsedAt() != null
                || userToken.getVerifiedAt() != null
                || userToken.getAttempts() >= MAX_ATTEMPTS
                || !LocalDateTime.now().isBefore(userToken.getExpiresAt())){
            throw new InvalidResetTokenException("Este token não é válido.");
        }

        if (!forgotPasswordTokenPolicy.matchHash(userToken.getHashToken(), code)){
            userToken.setAttempts(userToken.getAttempts() + 1);
            passwordResetTokenRepository.save(userToken);
            throw new InvalidResetTokenException("Este token não é válido.");
        }

        return userToken;
    }

    /** Validates the token issued by verifyResetCode, which proves the e-mailed code was already checked. */
    private PasswordResetToken checkResetToken(User user, String resetToken){
        PasswordResetToken userToken = findToken(user.getId()).get();

        if (userToken.getUsedAt() != null
                || userToken.getResetTokenHash() == null
                || userToken.getResetTokenExpiresAt() == null
                || !LocalDateTime.now().isBefore(userToken.getResetTokenExpiresAt())
                || !forgotPasswordTokenPolicy.matchHash(userToken.getResetTokenHash(), resetToken)){
            throw new InvalidResetTokenException("Este token não é válido.");
        }

        return userToken;
    }

    public VerifyResetCodeResponse verifyResetCode(VerifyResetCodeRequest request){
        User user = findByEmail(sanitizer.sanitizeEmail(request.email()))
                .orElseThrow(() -> new InvalidResetTokenException("Este token não é válido."));

        PasswordResetToken token = checkCode(user, request.token());

        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        String resetToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        token.setVerifiedAt(LocalDateTime.now());
        token.setResetTokenHash(forgotPasswordTokenPolicy.hashToken(resetToken));
        token.setResetTokenExpiresAt(LocalDateTime.now().plusMinutes(expiresAtMin));
        passwordResetTokenRepository.save(token);

        return new VerifyResetCodeResponse(resetToken, expiresAtMin * 60L);
    }

    public void resetPassword(ResetPasswordRequest resetPasswordRequest){
        String emailSanitizer = sanitizer.sanitizeEmail(resetPasswordRequest.email());

        Optional<User> haveUser = findByEmail(emailSanitizer);

        if (haveUser.isEmpty()) {
            throw new InvalidResetTokenException("Este token não é válido.");
        }

        PasswordResetToken checkedToken = checkResetToken(haveUser.get(), resetPasswordRequest.resetToken());

        boolean passwordEqualToOld = passwordPolicy.equalsPassword(haveUser.get(), resetPasswordRequest.newPassword());

        if (passwordEqualToOld){
            throw new PasswordSimilarToOldOneException("Esta senha é parecida com a anterior.");
        }

        String hashPassword = passwordPolicy.validPassword(resetPasswordRequest.newPassword());

        User user = haveUser.get();

        user.setPassword(hashPassword);

        userRepository.save(user);

        PasswordResetToken passwordResetToken = checkedToken;

        passwordResetToken.setUsedAt(LocalDateTime.now());
        passwordResetToken.setResetTokenHash(null);
        passwordResetToken.setResetTokenExpiresAt(null);

        passwordResetTokenRepository.save(passwordResetToken);

        

    }
}
