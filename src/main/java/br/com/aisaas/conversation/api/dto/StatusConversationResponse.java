package br.com.aisaas.conversation.api.dto;

import br.com.aisaas.conversation.domain.ConversationStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

public record StatusConversationResponse(@Enumerated(EnumType.STRING) ConversationStatus conversationStatus) {
}
