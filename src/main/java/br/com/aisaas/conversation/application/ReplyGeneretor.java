package br.com.aisaas.conversation.application;


import br.com.aisaas.agent.domain.Agent;
import br.com.aisaas.conversation.domain.exceptions.GeminiAPINotRespondException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;


@Component
public class ReplyGeneretor {
    private final ChatClient chatClient;

    public ReplyGeneretor(ChatModel chatModel) {
        this.chatClient = ChatClient.create(chatModel);
    }

    public Flux<ChatResponse> finalMessage(Agent agentRouter, List<org.springframework.ai.chat.messages.Message> fiveLastMessage){

        return chatClient.prompt()
                .options(GoogleGenAiChatOptions.builder()
                        .model(agentRouter.getModelName())
                        .temperature(agentRouter.getTemperature())
                        .maxOutputTokens(agentRouter.getMaxTokens()))
                .system(agentRouter.getSystemPrompt())
                .messages(fiveLastMessage)
                .stream()
                .chatResponse()
                .onErrorMap(e -> new GeminiAPINotRespondException(e.getMessage()));
    }
}
