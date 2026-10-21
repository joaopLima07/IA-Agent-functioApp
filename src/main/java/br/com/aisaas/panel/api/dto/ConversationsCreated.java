package br.com.aisaas.panel.api.dto;

import java.time.LocalDate;

public record ConversationsCreated(LocalDate date, Long quantityConversations) {
}
