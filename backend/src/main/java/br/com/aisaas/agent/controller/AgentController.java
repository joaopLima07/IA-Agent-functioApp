package br.com.aisaas.agent.controller;

import br.com.aisaas.agent.application.AgentQueryService;
import br.com.aisaas.agent.controller.dto.AgentDetailResponse;
import br.com.aisaas.agent.controller.dto.AgentSummaryResponse;
import br.com.aisaas.agent.domain.Agent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
public class AgentController {
    private final AgentQueryService agentQueryService;


    public AgentController(AgentQueryService agentQueryService) {
        this.agentQueryService = agentQueryService;
    }

    @GetMapping("/api/agents")
    public ResponseEntity<List<AgentSummaryResponse>> listAllAgent(){
        return ResponseEntity.ok().body(agentQueryService.searchAll().stream()
                .map(a -> new AgentSummaryResponse(
                        a.getId(),
                        a.getName(),
                        a.getDescription(),
                        a.getStatus(),
                        a.getModelName(),
                        a.getUpdatedAt()))
                .toList());
    }

    @GetMapping("/api/agents/{id}")
    public ResponseEntity<AgentDetailResponse> getAgent(@PathVariable long id){
        Agent agentUnique = agentQueryService.searchUnique(id);

        return ResponseEntity.ok().body(new AgentDetailResponse(agentUnique.getId(),
                agentUnique.getCreatedAt(),
                agentUnique.getUpdatedAt(),
                agentUnique.getVersion(),
                new AgentDetailResponse.Information(agentUnique.getName(),
                        agentUnique.getDescription(),
                        agentUnique.getStatus()),
                new AgentDetailResponse.Model(agentUnique.getModelProvider(),
                        agentUnique.getModelName(),
                        agentUnique.getTemperature(),
                        agentUnique.getMaxTokens()
                ),
                agentUnique.getSystemPrompt(),
                agentUnique.getVisibility()
        ));
    }
}
