package br.com.aisaas.agent.controller.dto;

import br.com.aisaas.agent.domain.AgentStatus;
import br.com.aisaas.agent.domain.AgentVisibility;

import java.time.LocalDateTime;

public record AgentDetailResponse(
        long id,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Integer version,
        Information information,
        Model model,
        String systemPrompt,
        AgentVisibility visibility
    ) {
    public record Information(
            String name,
            String description,
            AgentStatus status
    ){}

    public record Model(
            String modelProvider,
            String modelName,
            double temperature,
            Integer maxTokens
    ){}
}
