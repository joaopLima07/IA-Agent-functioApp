package br.com.aisaas.conversation.application;


import br.com.aisaas.agent.domain.Agent;
import br.com.aisaas.conversation.domain.exceptions.GeminiAPINotRespondException;
import br.com.aisaas.conversation.tools.AllTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;
// TODO
// Para o chat devolver imagem, há dois caminhos:
// 1. Imagem por URL (o mais simples): uma tool, como a AllTools, gera ou busca a imagem, sobe para um armazenamento
// (por exemplo, Azure Blob Storage) e devolve a URL. O agente a inclui na resposta em Markdown, e o frontend já renderiza. Quase não muda o backend.
// 2. Imagem gerada pelo próprio modelo: exige um modelo com saída de imagem no Gemini e tratar a parte de mídia da resposta
// no ChatService em vez de só getText(). Seria preciso um novo tipo de evento SSE e um campo de anexo na mensagem. O frontend também precisaria de um componente para exibir isso, e o histórico carregado depois teria que trazer a imagem.

@Component
public class ReplyGeneretor {
    private final ChatClient chatClient;
    private final AllTools allTools;

    public ReplyGeneretor(ChatModel chatModel, AllTools allTools) {
        this.chatClient = ChatClient.create(chatModel);
        this.allTools = allTools;
    }

    public Flux<ChatResponse> finalMessage(Agent agentRouter, List<org.springframework.ai.chat.messages.Message> fiveLastMessage){

        return chatClient.prompt()
                .options(GoogleGenAiChatOptions.builder()
                        .model(agentRouter.getModelName())
                        .temperature(agentRouter.getTemperature())
                        .maxOutputTokens(agentRouter.getMaxTokens()))
                .system(agentRouter.getSystemPrompt())
                .messages(fiveLastMessage)
                .tools(allTools)
                .stream()
                .chatResponse()
                .onErrorMap(e -> new GeminiAPINotRespondException(e.getMessage()));
    }
}
