package br.com.aisaas.conversation.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RenameConversationRequest(@NotBlank @Size(max = 100) String title) {
}
