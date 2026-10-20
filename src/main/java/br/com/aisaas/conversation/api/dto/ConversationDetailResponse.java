package br.com.aisaas.conversation.api.dto;
import br.com.aisaas.conversation.domain.ConversationStatus;
import br.com.aisaas.conversation.domain.MessageRole;

import java.time.LocalDateTime;
import java.util.List;

public record ConversationDetailResponse(long id,
                                         long userId,
                                         String title,
                                         ConversationStatus status,
                                         List<MessagesResponse> messages) {

    public record MessagesResponse(long id,
                                   long conversationId,
                                   MessageRole role,
                                   Long agentId,
                                   String content,
                                   LocalDateTime createdAt){}
}
