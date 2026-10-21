package br.com.aisaas.panel.api.dto;

import java.time.LocalDate;

public record ResponseTimeResponse(LocalDate date, double latencyMs) {
}
