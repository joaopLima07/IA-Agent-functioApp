package br.com.aisaas.agent.infra;

import br.com.aisaas.agent.domain.Agent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AgentRepository extends JpaRepository<Agent, Long> {
    @Query("select a from agent a where a.visibility = 'PUBLIC'")
    public List<Agent> findAllAgentPublic();

    @Query("select a from agent a where a.id = 10")
    public Agent findAgentDecision();
}
