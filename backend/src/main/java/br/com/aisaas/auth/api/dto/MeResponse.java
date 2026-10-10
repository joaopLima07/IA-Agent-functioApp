package br.com.aisaas.auth.api.dto;

public record MeResponse(long id, String fullName, String email, String phone, String role) {
}
