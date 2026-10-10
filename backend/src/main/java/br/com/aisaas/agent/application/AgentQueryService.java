package br.com.aisaas.agent.application;

import br.com.aisaas.agent.api.dto.UpdateAgentRequest;
import br.com.aisaas.agent.domain.Agent;
import br.com.aisaas.agent.domain.exceptions.AgentNotFoundException;
import br.com.aisaas.agent.domain.exceptions.InvalidAgentConfigException;
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

    public Agent updateAgent(long id, UpdateAgentRequest updateAgentRequest){
        Optional<Agent> haveAgent = searchById(id);

        if (updateAgentRequest.model().temperature() > 2){
            throw new InvalidAgentConfigException("The Temperature is upper than valid.");
        }

        if (!haveAgent.get().getVersion().equals(updateAgentRequest.version())){
            throw new InvalidAgentConfigException("This agent already modificated by another person, reload.");
        }

        Agent agent = haveAgent.get();

        agent.setName(updateAgentRequest.information().name());
        agent.setDescription(updateAgentRequest.information().description());
        agent.setStatus(updateAgentRequest.information().status());
        agent.setModelProvider(updateAgentRequest.model().modelProvider());
        agent.setModelName(updateAgentRequest.model().modelName());
        agent.setTemperature(updateAgentRequest.model().temperature());
        agent.setMaxTokens(updateAgentRequest.model().maxTokens());
        agent.setSystemPrompt(updateAgentRequest.systemPrompt());
        agent.setVisibility(updateAgentRequest.visibility());

        return agentRepository.save(agent);
    }
}


