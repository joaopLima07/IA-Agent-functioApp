package br.com.aisaas.agent.infra;

import br.com.aisaas.agent.domain.Agent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentRepository extends JpaRepository<Agent, Long> {}
