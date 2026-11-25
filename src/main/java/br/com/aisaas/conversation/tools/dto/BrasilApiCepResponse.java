package br.com.aisaas.conversation.tools.dto;

public record BrasilApiCepResponse(
        String cep,
        String street,
        String neighborhood,
        String city,
        String state
) {
}
