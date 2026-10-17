package br.com.aisaas.auth.application;

import br.com.aisaas.auth.domain.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class JwtTokenService {
    private final JwtEncoder jwtEncoder;
    private final long expirationSeconds;

    public JwtTokenService(JwtEncoder jwtEncoder, @Value("${jwt.expiration-seconds:900}") long expirationSeconds) {
        this.jwtEncoder = jwtEncoder;
        this.expirationSeconds = expirationSeconds;
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }

    public String generateToken(User user){
        Instant now = Instant.now();

        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .subject(String.valueOf(user.getId()))
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expirationSeconds))
                .claim("role", user.getRole().name());

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims.build())).getTokenValue();
    }
}
