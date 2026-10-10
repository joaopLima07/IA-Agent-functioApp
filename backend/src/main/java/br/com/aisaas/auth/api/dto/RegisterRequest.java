package br.com.aisaas.auth.api.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record RegisterRequest(@NotBlank @NotEmpty String fullName, @NotBlank @NotEmpty @Email String email, @NotBlank @NotEmpty String password, @NotBlank @NotEmpty String phone) {}
