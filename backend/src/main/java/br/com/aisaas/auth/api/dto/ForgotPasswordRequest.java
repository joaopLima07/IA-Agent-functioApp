package br.com.aisaas.auth.api.dto;

import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(@NotBlank(message = "O e-mail é obrigatório") String email) {
}
