package br.com.aisaas.conversation.application;

import br.com.aisaas.agent.domain.Agent;
import br.com.aisaas.conversation.domain.exceptions.GeminiAPINotRespondException;
import br.com.aisaas.conversation.domain.exceptions.InvalidRouterResponseException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AgentRouter {
    private final ChatClient chatClient;


    public AgentRouter(ChatModel chatModel) {
        this.chatClient = ChatClient.create(chatModel);
    }


    public ChatResponse choseAgent(Agent routerAgent, String promptFiveLastMessage, String allAgents){

        try{
            return chatClient.prompt()
                    .options(GoogleGenAiChatOptions.builder()
                            .model(routerAgent.getModelName())
                            .temperature(routerAgent.getTemperature()))
                    .system(routerAgent.getSystemPrompt())
                    .user(u -> u.text("""
                            Agentes Disponiveis:
                            {agent}

                            Ultimas 5 mensagens:
                            {message}""")
                            .param("agent", allAgents)
                            .param("message", promptFiveLastMessage))
                    .call()
                    .chatResponse();

        } catch (Exception e){
            throw new GeminiAPINotRespondException("The Gemini API not respond", e);
        }

    }
}
