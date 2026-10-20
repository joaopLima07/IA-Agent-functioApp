package br.com.aisaas.conversation.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AlterTitleRequest(@NotNull long id, @NotBlank String oldTitle, @NotBlank String newTitle){
}
