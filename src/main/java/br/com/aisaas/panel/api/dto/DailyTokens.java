package br.com.aisaas.panel.api.dto;

import java.time.LocalDate;

public record DailyTokens(LocalDate date, Long totalTokens) {
}
