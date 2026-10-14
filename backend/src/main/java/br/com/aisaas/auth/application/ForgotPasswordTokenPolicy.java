package br.com.aisaas.auth.application;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class ForgotPasswordTokenPolicy {
    private final PasswordEncoder passwordEncoder;

    public ForgotPasswordTokenPolicy(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public String hashToken(String token){
        return passwordEncoder.encode(token);
    }

    public boolean matchHash(String hashToken, String token){
        return passwordEncoder.matches(token, hashToken);
    }
}
