package br.com.aisaas.conversation.application;


import br.com.aisaas.agent.domain.Agent;
import br.com.aisaas.conversation.application.dto.ReplyResult;
import br.com.aisaas.conversation.domain.exceptions.GeminiAPINotRespondException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.stereotype.Component;


@Component
public class ReplyGeneretor {
    private final ChatClient chatClient;

    public ReplyGeneretor(ChatModel chatModel) {
        this.chatClient = ChatClient.create(chatModel);
    }

    public ReplyResult finalMessage(Agent agentRouter, String fiveLastMessage){
        long start = System.nanoTime();

        try {
            ChatResponse chatResponse = chatClient.prompt()
                    .options(GoogleGenAiChatOptions.builder()
                            .model(agentRouter.getModelName())
                            .temperature(agentRouter.getTemperature()))
                    .system(agentRouter.getSystemPrompt())
                    .user(u -> u.text("""
                            Ultimas 5 mensagens: 
                            {message}
                            """)
                            .param("message", fiveLastMessage))
                    .call()
                    .chatResponse();
            int latencyMs = (int) ((System.nanoTime() - start) / 1_000_000);

            Usage usage = chatResponse.getMetadata().getUsage();

            int output = usage.getCompletionTokens() != null ? usage.getCompletionTokens() : 0;
            int input = usage.getPromptTokens() != null ? usage.getPromptTokens() : 0;

            return new ReplyResult(chatResponse.getResult().getOutput().getText(), output, input, latencyMs);

        } catch (Exception e){
            throw new GeminiAPINotRespondException(e.getMessage());
        }
    }
}
