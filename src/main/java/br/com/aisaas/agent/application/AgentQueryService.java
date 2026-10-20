package br.com.aisaas.agent.application;

import br.com.aisaas.agent.api.dto.UpdateAgentRequest;
import br.com.aisaas.agent.domain.Agent;
import br.com.aisaas.agent.domain.exceptions.AgentNotFoundException;
import br.com.aisaas.agent.domain.exceptions.InvalidAgentConfigException;
import br.com.aisaas.agent.infra.AgentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AgentQueryService {
    private final AgentRepository agentRepository;

    public AgentQueryService(AgentRepository agentRepository) {
        this.agentRepository = agentRepository;
    }

    Agent searchById(long id){
        return agentRepository.findById(id).orElseThrow(() -> new AgentNotFoundException("Agente não encontrado com o id " + id));
    }

    public List<Agent> searchAll(){
        return agentRepository.findAllAgentPublic();
    }

    public Agent findRouterAgent(){
        return agentRepository.findAgentDecision();
    }

    public Agent searchUnique(long id){
       return searchById(id);
    }

    public Agent updateAgent(long id, UpdateAgentRequest updateAgentRequest){
        Agent agent = searchById(id);

        if (updateAgentRequest.model().temperature() > 2){
            throw new InvalidAgentConfigException(InvalidAgentConfigException.TEMPERATURE_TOO_HIGH);
        }

        if (!agent.getVersion().equals(updateAgentRequest.version())){
            throw new InvalidAgentConfigException("Este agente já foi modificado por outra pessoa, recarregue a página.");
        }

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


