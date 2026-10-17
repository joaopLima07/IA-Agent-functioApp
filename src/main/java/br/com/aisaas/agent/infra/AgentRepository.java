package br.com.aisaas.agent.infra;

import br.com.aisaas.agent.domain.Agent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AgentRepository extends JpaRepository<Agent, Long> {
    public Optional<Agent> findByName(String name);
}
