package br.com.aisaas.auth.infra;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

@ConfigurationProperties("refresh-token")
public record RefreshTokenProperties(
        @DefaultValue("7d") Duration ttl,
        @DefaultValue("true") boolean cookieSecure,
        @DefaultValue("Lax") String cookieSameSite,
        @DefaultValue("/api/auth") String cookiePath
) {
}
