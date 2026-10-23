package br.com.aisaas.conversation.api.dto;

import jakarta.validation.constraints.NotBlank;

public record AlterTitleRequest(@NotBlank String newTitle){
}
