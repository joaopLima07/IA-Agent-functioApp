package br.com.aisaas.panel.api.dto;

import java.time.LocalDate;

public record AgentUsageResponse(LocalDate date, long agentId, long totalUsage) {
}
