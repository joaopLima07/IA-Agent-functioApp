package br.com.aisaas.conversation.api.dto;

import br.com.aisaas.conversation.domain.ConversationStatus;


public record ConversationSummaryResponse(long id,
                                          long userId,
                                          String title,
                                          ConversationStatus status,
                                          String lastMessage) {

    public ConversationSummaryResponse(long id, long userId, String title, ConversationStatus status) {
        this(id, userId, title, status, null);
    }
}
