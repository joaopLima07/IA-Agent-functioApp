package br.com.aisaas.conversation.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SendMessageRequest(@NotNull long id,
                                 @NotNull @Size(max = 500) String content) {
}
