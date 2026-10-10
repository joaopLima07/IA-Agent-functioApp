package br.com.aisaas.agent.controller.dto;

import br.com.aisaas.agent.domain.AgentStatus;

import java.time.LocalDateTime;

public record AgentSummaryResponse(
        long id,
        String name,
        String description,
        AgentStatus status,
        String modelName,
        LocalDateTime updatedAt
) {}
