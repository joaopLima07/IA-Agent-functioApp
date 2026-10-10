package br.com.aisaas.auth.application;

import br.com.aisaas.auth.domain.User;
import br.com.aisaas.auth.domain.UserRole;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenServiceTest {

    private static final long EXPIRATION_SECONDS = 600;

    private SecretKey key;
    private JwtDecoder decoder;
    private JwtTokenService service;

    @BeforeEach
    void setUp() {
        key = new SecretKeySpec("test-secret-with-at-least-32-bytes!!".getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        decoder = NimbusJwtDecoder.withSecretKey(key).build();
        service = new JwtTokenService(new NimbusJwtEncoder(new ImmutableSecret<>(key)), EXPIRATION_SECONDS);
    }

    private User user() {
        User user = new User();
        user.setId(7L);
        user.setRole(UserRole.ADMIN);
        return user;
    }

    @Test
    void generatesSignedTokenWithSubjectAndRole() {
        Jwt jwt = decoder.decode(service.generateToken(user()));

        assertEquals("7", jwt.getSubject());
        assertEquals("ADMIN", jwt.getClaimAsString("role"));
        assertEquals("HS256", jwt.getHeaders().get("alg"));
    }

    @Test
    void expiresAfterConfiguredSeconds() {
        Jwt jwt = decoder.decode(service.generateToken(user()));

        assertEquals(Duration.ofSeconds(EXPIRATION_SECONDS), Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt()));
        assertEquals(EXPIRATION_SECONDS, service.getExpirationSeconds());
    }

    @Test
    void tokenSignedWithAnotherKeyIsRejected() {
        SecretKey otherKey = new SecretKeySpec("another-secret-with-32-bytes-min!!!".getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        String token = service.generateToken(user());

        assertThrows(JwtException.class, () -> NimbusJwtDecoder.withSecretKey(otherKey).build().decode(token));
    }
}
