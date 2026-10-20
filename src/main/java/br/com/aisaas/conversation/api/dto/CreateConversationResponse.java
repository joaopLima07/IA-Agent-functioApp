package br.com.aisaas.conversation.api.dto;

import br.com.aisaas.conversation.domain.ConversationStatus;

import java.time.LocalDateTime;

public record CreateConversationResponse(long id,
                                         String title,
                                         ConversationStatus status,
                                         LocalDateTime createdAt) {
}
