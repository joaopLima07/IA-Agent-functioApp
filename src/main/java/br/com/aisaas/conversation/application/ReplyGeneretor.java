package br.com.aisaas.conversation.application;


import br.com.aisaas.agent.domain.Agent;
import br.com.aisaas.conversation.domain.exceptions.GeminiAPINotRespondException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;


@Component
public class ReplyGeneretor {
    private static final Logger log = LoggerFactory.getLogger(ReplyGeneretor.class);
    private final ChatClient chatClient;

    public ReplyGeneretor(ChatModel chatModel) {
        this.chatClient = ChatClient.create(chatModel);
    }

    public Flux<ChatResponse> finalMessage(Agent agentRouter, List<org.springframework.ai.chat.messages.Message> fiveLastMessage){

        log.info("Gemini request: agent={} model={} temperature={} maxOutputTokens={} messages={}",
                agentRouter.getId(), agentRouter.getModelName(), agentRouter.getTemperature(), agentRouter.getMaxTokens(),
                fiveLastMessage.stream().map(ReplyGeneretor::describe).toList());

        return chatClient.prompt()
                .options(GoogleGenAiChatOptions.builder()
                        .model(agentRouter.getModelName())
                        .temperature(agentRouter.getTemperature())
                        .maxOutputTokens(agentRouter.getMaxTokens()))
                .system(agentRouter.getSystemPrompt())
                .messages(fiveLastMessage)
                .stream()
                .chatResponse()
                .onErrorMap(e -> new GeminiAPINotRespondException(e.getMessage(), e));
    }

    // resumo de cada mensagem para o log, sem o conteúdo: papel, tamanho do texto e tipo/tamanho das mídias
    private static String describe(org.springframework.ai.chat.messages.Message m){
        String text = m.getText();
        String summary = m.getMessageType() + "(" + (text == null ? "null" : text.length() + " chars");
        if (m instanceof UserMessage user && !user.getMedia().isEmpty()){
            summary += ", media=" + user.getMedia().stream()
                    .map(media -> media.getMimeType() + " " + (media.getData() instanceof byte[] b ? b.length + " bytes" : "?"))
                    .toList();
        }
        return summary + ")";
    }
}
