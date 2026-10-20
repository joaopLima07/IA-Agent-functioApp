package br.com.aisaas.conversation.api.dto;

import br.com.aisaas.conversation.domain.ConversationStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AlterStatusConversationRequest(@NotNull long id, @NotNull @Enumerated(EnumType.STRING)ConversationStatus status) {
}
