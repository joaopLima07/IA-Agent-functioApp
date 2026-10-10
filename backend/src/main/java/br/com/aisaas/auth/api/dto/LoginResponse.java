package br.com.aisaas.auth.api.dto;

public record LoginResponse(String accessToken, long expiresIn) {
}
