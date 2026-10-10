package br.com.aisaas.agent.api.dto;

import br.com.aisaas.agent.domain.AgentStatus;
import br.com.aisaas.agent.domain.AgentVisibility;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public record UpdateAgentRequest(
        @NotNull Integer version,
        @NotNull @Valid AgentDetailResponse.Information information,
        @NotNull @Valid AgentDetailResponse.Model model,
        @NotBlank String systemPrompt,
        @NotNull AgentVisibility visibility
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

