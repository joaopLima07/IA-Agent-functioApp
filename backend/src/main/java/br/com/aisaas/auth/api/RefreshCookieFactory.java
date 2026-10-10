package br.com.aisaas.auth.api;

import br.com.aisaas.auth.infra.RefreshTokenProperties;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RefreshCookieFactory {
    public static final String COOKIE_NAME = "refresh_token";

    private final RefreshTokenProperties properties;

    public RefreshCookieFactory(RefreshTokenProperties properties) {
        this.properties = properties;
    }

    public ResponseCookie create(String token) {
        return build(token, properties.ttl());
    }

    public ResponseCookie clear() {
        return build("", Duration.ZERO);
    }

    private ResponseCookie build(String value, Duration maxAge) {
        return ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)
                .secure(properties.cookieSecure())
                .sameSite(properties.cookieSameSite())
                .path(properties.cookiePath())
                .maxAge(maxAge)
                .build();
    }
}
