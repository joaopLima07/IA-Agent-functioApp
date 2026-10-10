package br.com.aisaas.auth.application;

public record AuthTokens(String accessToken, long expiresIn, String refreshToken) {
}
