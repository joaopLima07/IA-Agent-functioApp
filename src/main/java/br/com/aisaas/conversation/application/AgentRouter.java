package br.com.aisaas.conversation.application;

import br.com.aisaas.agent.domain.Agent;
import br.com.aisaas.conversation.domain.exceptions.GeminiAPINotRespondException;
import br.com.aisaas.conversation.domain.exceptions.InvalidRouterResponseException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.stereotype.Component;

@Component
public class AgentRouter {
    private final ChatClient chatClient;


    public AgentRouter(ChatModel chatModel) {
        this.chatClient = ChatClient.create(chatModel);
    }


    public long choseAgent(Agent routerAgent, String promptFiveLastMessage, String allAgents){

        String response;

        try{
            response = chatClient.prompt()
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
                    .content();
        } catch (Exception e){
            throw new GeminiAPINotRespondException("The Gemini API not respond");
        }

        return parseAgentId(response);
    }

    private long parseAgentId(String response){
        if (response == null || response.isBlank()){
            throw new InvalidRouterResponseException("The router returned an empty response.");
        }

        try {
            return Long.parseLong(response.trim());
        } catch (NumberFormatException e){
            throw new InvalidRouterResponseException("The router did not return a valid agent id: " + response);
        }
    }
}
