package br.com.aisaas.conversation.application.dto;

public record ReplyResult (String content, int outputToken, int inputToken, int latencyMs){
}
