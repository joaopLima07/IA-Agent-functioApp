package br.com.aisaas.auth.api.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record RegisterRequest(
        @NotBlank(message = "O nome completo é obrigatório") @NotEmpty(message = "O nome completo é obrigatório") String fullName,
        @NotBlank(message = "O e-mail é obrigatório") @NotEmpty(message = "O e-mail é obrigatório") @Email(message = "Informe um e-mail válido") String email,
        @NotBlank(message = "A senha é obrigatória") @NotEmpty(message = "A senha é obrigatória") String password,
        @NotBlank(message = "O telefone é obrigatório") @NotEmpty(message = "O telefone é obrigatório") String phone) {}
