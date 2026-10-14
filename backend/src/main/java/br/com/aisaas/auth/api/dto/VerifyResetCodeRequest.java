package br.com.aisaas.auth.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyResetCodeRequest(
        @NotBlank(message = "O e-mail é obrigatório") String email,
        @NotBlank(message = "O código é obrigatório") @Pattern(regexp = "\\d{6}", message = "O código deve ter 6 números.") String token) {
}
