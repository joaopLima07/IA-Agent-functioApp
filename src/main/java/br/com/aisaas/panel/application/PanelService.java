package br.com.aisaas.panel.application;

import br.com.aisaas.panel.api.dto.*;
import br.com.aisaas.panel.infra.PanelRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PanelService {
    private final PanelRepository panelRepository;

    public PanelService(PanelRepository dailyTokenRepository) {
        this.panelRepository = dailyTokenRepository;
    }

    public List<DailyTokens> getDailyTokens(){
        return panelRepository.dailyToken();
    }

    public List<ConversationsCreated> conversationsCreateds(){
        return panelRepository.convesationsCreated();
    }

    public CardsResponse cardsResponses(){
        return panelRepository.cards();
    }

    public List<AgentUsageResponse> agentUsage(){
        return panelRepository.agentUsage();
    }

    public List<ResponseTimeResponse> responseTime(){
        return panelRepository.responseTime();
    }
}
