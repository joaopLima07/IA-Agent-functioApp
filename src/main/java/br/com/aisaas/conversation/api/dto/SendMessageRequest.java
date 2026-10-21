package br.com.aisaas.conversation.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SendMessageRequest(@NotNull @Size(max = 10000) String content) {
}
