package br.com.aisaas.auth.api.dto;

public record VerifyResetCodeResponse(String resetToken, long expiresIn) {
}
