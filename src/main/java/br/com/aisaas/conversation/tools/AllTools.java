package br.com.aisaas.conversation.tools;

import br.com.aisaas.agent.application.AgentQueryService;
import br.com.aisaas.agent.domain.Agent;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AllTools {
    private final AgentQueryService agentQueryService;

    public AllTools(AgentQueryService agentQueryService) {
        this.agentQueryService = agentQueryService;
    }

    @Tool(description = "Lista todos os agentes disponíveis na plataforma, com o nome e a descrição "
            + "de cada um (o que faz e como usar). Chame sempre antes de explicar ou recomendar agentes.")
    public String listAllAgentsDescriptions(){
        List<Agent> haveAgents = agentQueryService.searchAll();

        if (haveAgents.isEmpty()){
            return "Não tem agente disponivel";
        }

        StringBuilder allAgentsMessage = new StringBuilder();

        for (Agent a : haveAgents){
            allAgentsMessage.append("- ").append(a.getName())
                    .append(": ").append(a.getDescription())
                    .append("\n");
        }

        return allAgentsMessage.toString();
    }
}
