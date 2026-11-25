package br.com.aisaas.conversation.tools.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PriceResponse(
        String code,
        String bid,
        String ask,
        String pctChange,
        @JsonProperty("create_date") String createDate
) {
}