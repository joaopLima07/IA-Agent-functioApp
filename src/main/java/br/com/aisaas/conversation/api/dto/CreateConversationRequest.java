package br.com.aisaas.conversation.api.dto;

import br.com.aisaas.conversation.domain.ConversationStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateConversationRequest (@NotNull @Size(max = 100) String title, @NotNull @Enumerated(EnumType.STRING) ConversationStatus status){
}
