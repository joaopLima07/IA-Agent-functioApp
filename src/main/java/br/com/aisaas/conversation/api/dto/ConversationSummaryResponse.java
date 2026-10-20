package br.com.aisaas.conversation.api.dto;

import br.com.aisaas.conversation.domain.Conversation;
import br.com.aisaas.conversation.domain.ConversationStatus;

import java.util.List;

public record ConversationSummaryResponse(long id,
                                          long userId,
                                          String title,
                                          ConversationStatus status) {
}
