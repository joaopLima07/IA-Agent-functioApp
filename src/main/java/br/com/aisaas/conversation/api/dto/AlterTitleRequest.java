package br.com.aisaas.conversation.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AlterTitleRequest(@NotBlank String newTitle){
}
