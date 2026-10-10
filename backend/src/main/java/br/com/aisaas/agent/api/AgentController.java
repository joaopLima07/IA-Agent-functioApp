package br.com.aisaas.agent.api;

import br.com.aisaas.agent.application.AgentQueryService;
import br.com.aisaas.agent.api.dto.AgentDetailResponse;
import br.com.aisaas.agent.api.dto.AgentSummaryResponse;
import br.com.aisaas.agent.api.dto.UpdateAgentRequest;
import br.com.aisaas.agent.domain.Agent;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
public class AgentController {
    private final AgentQueryService agentQueryService;


    public AgentController(AgentQueryService agentQueryService) {
        this.agentQueryService = agentQueryService;
    }

    AgentDetailResponse mountAgentDetail(Agent agent){
        return new AgentDetailResponse(
                agent.getId(),
                agent.getCreatedAt(),
                agent.getUpdatedAt(),
                agent.getVersion(),
                new AgentDetailResponse.Information(
                        agent.getName(),
                        agent.getDescription(),
                        agent.getStatus()
                ),
                new AgentDetailResponse.Model(
                        agent.getModelProvider(),
                        agent.getModelName(),
                        agent.getTemperature(),
                        agent.getMaxTokens()
                ),
                agent.getSystemPrompt(),
                agent.getVisibility()
        );
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

        return ResponseEntity.ok().body(mountAgentDetail(agentUnique));
    }

    @PutMapping("/api/agents/{id}")
    public ResponseEntity<AgentDetailResponse> updateAgent(@PathVariable long id, @Valid @RequestBody UpdateAgentRequest updateAgentRequest){
        Agent agentResponse = agentQueryService.updateAgent(id, updateAgentRequest);

        return ResponseEntity.ok().body(mountAgentDetail(agentResponse));
    }
}
