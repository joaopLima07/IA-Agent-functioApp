package br.com.aisaas.agent.api.dto;

import br.com.aisaas.agent.domain.AgentStatus;
import br.com.aisaas.agent.domain.AgentVisibility;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public record UpdateAgentRequest(
        @NotNull(message = "A versão é obrigatória") Integer version,
        @NotNull(message = "As informações do agente são obrigatórias") @Valid AgentDetailResponse.Information information,
        @NotNull(message = "A configuração do modelo é obrigatória") @Valid AgentDetailResponse.Model model,
        @NotBlank(message = "O prompt do sistema é obrigatório") String systemPrompt,
        @NotNull(message = "A visibilidade é obrigatória") AgentVisibility visibility
        ) {
    public record Information(
            @NotBlank String name,
            @NotBlank String description,
            @NotNull AgentStatus status
    ){}

    public record Model(
            @NotBlank String modelProvider,
            @NotNull String modelName,
            @NotNull @DecimalMin(value = "0.0") @DecimalMax(value = "2.0") double temperature,
            @Positive Integer maxTokens
    ) {}
}

