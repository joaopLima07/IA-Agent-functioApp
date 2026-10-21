package br.com.aisaas.panel.api;

import br.com.aisaas.panel.api.dto.*;
import br.com.aisaas.panel.application.PanelService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PanelController {
    private final PanelService panelService;

    public PanelController(PanelService panelService) {
        this.panelService = panelService;
    }

    @GetMapping("/api/panel/dailyTokens")
    public ResponseEntity<List<DailyTokens>> dailyTokens(){
        return ResponseEntity.ok().body(panelService.getDailyTokens());
    }

    @GetMapping("/api/panel/conversationsCreated")
    public ResponseEntity<List<ConversationsCreated>> conversationsCreated(){
        return ResponseEntity.ok().body(panelService.conversationsCreateds());
    }

    @GetMapping("/api/panel/cards")
    public ResponseEntity<CardsResponse> cards(){
        return ResponseEntity.ok().body(panelService.cardsResponses());
    }

    @GetMapping("/api/panel/agentUsage")
    public ResponseEntity<List<AgentUsageResponse>> agentUsage(){
        return ResponseEntity.ok().body(panelService.agentUsage());
    }

    @GetMapping("/api/panel/responseTime")
    public ResponseEntity<List<ResponseTimeResponse>> responseTime(){
        return ResponseEntity.ok().body(panelService.responseTime());
    }
}
