package br.com.aisaas.auth.application;

import br.com.aisaas.auth.domain.RefreshToken;
import br.com.aisaas.auth.domain.User;
import br.com.aisaas.auth.domain.UserStatus;
import br.com.aisaas.auth.domain.exceptions.InvalidRefreshTokenException;
import br.com.aisaas.auth.infra.RefreshTokenProperties;
import br.com.aisaas.auth.infra.RefreshTokenRepository;
import br.com.aisaas.auth.infra.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class RefreshTokenService {
    private static final String INVALID = "Invalid refresh token.";
    private static final int TOKEN_BYTES = 32;

    private final RefreshTokenRepository repository;
    private final UserRepository userRepository;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenService(RefreshTokenRepository repository, UserRepository userRepository,
                               JwtTokenService jwtTokenService, RefreshTokenProperties properties) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.jwtTokenService = jwtTokenService;
        this.properties = properties;
    }

    /** Starts a new session (new family) and returns the raw refresh token. */
    @Transactional
    public String issue(User user) {
        return create(user.getId(), UUID.randomUUID()).raw();
    }

    /**
     * Exchanges a refresh token for a new pair. Reuse of a revoked token revokes the whole family;
     * noRollbackFor keeps that revocation committed even though we throw.
     */
    @Transactional(noRollbackFor = InvalidRefreshTokenException.class)
    public AuthTokens rotate(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new InvalidRefreshTokenException(INVALID);
        }

        Instant now = Instant.now();

        RefreshToken current = repository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new InvalidRefreshTokenException(INVALID));

        if (current.isRevoked()) {
            repository.revokeFamily(current.getFamilyId(), now);
            throw new InvalidRefreshTokenException(INVALID);
        }

        if (current.isExpired(now)) {
            throw new InvalidRefreshTokenException(INVALID);
        }

        User user = userRepository.findById(current.getUserId())
                .filter(u -> u.getStatus() == UserStatus.ACTIVE)
                .orElseThrow(() -> new InvalidRefreshTokenException(INVALID));

        Issued next = create(user.getId(), current.getFamilyId());
        current.revoke(now, next.entity().getId());
        repository.save(current);

        return new AuthTokens(jwtTokenService.generateToken(user), jwtTokenService.getExpirationSeconds(), next.raw());
    }

    /** Idempotent: unknown or missing token is silently ignored. */
    @Transactional
    public void revoke(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return;
        }
        repository.findByTokenHash(hash(rawToken))
                .ifPresent(t -> repository.revokeFamily(t.getFamilyId(), Instant.now()));
    }

    private Issued create(long userId, UUID familyId) {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        RefreshToken entity = new RefreshToken(userId, hash(raw), familyId, Instant.now().plus(properties.ttl()));
        repository.save(entity);
        return new Issued(entity, raw);
    }

    private static String hash(String raw) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private record Issued(RefreshToken entity, String raw) {
    }
}
