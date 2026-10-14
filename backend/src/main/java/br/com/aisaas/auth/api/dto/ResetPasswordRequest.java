package br.com.aisaas.auth.api.dto;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(
        @NotBlank(message = "O e-mail é obrigatório") String email,
        @NotBlank(message = "O token de redefinição é obrigatório") String resetToken,
        @NotBlank(message = "A nova senha é obrigatória") String newPassword) {
}
