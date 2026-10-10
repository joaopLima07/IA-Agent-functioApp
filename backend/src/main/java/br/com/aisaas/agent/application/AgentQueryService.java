package br.com.aisaas.agent.application;

import br.com.aisaas.agent.controller.AgentController;
import br.com.aisaas.agent.domain.Agent;
import br.com.aisaas.agent.domain.exceptions.AgentNotFoundException;
import br.com.aisaas.agent.infra.AgentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AgentQueryService {
    private final AgentRepository agentRepository;

    public AgentQueryService(AgentRepository agentRepository) {
        this.agentRepository = agentRepository;
    }

    Optional<Agent> searchById(long userId){
        return Optional.of(agentRepository.findById(userId).orElseThrow(() -> new AgentNotFoundException("Not found agent with id " + userId)));
    }

    public List<Agent> searchAll(){
        return agentRepository.findAll();
    }

    public Agent searchUnique(long id){
        Optional<Agent> haveAgent = searchById(id);

       return haveAgent.get();
    }
}


