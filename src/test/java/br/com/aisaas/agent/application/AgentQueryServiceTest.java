package br.com.aisaas.agent.application;

import br.com.aisaas.agent.api.dto.AgentDetailResponse;
import br.com.aisaas.agent.api.dto.UpdateAgentRequest;
import br.com.aisaas.agent.domain.Agent;
import br.com.aisaas.agent.domain.AgentStatus;
import br.com.aisaas.agent.domain.AgentVisibility;
import br.com.aisaas.agent.domain.exceptions.AgentNotFoundException;
import br.com.aisaas.agent.domain.exceptions.InvalidAgentConfigException;
import br.com.aisaas.agent.infra.AgentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AgentQueryServiceTest {

    private AgentRepository repository;
    private AgentQueryService service;

    @BeforeEach
    void setUp() {
        repository = mock(AgentRepository.class);
        service = new AgentQueryService(repository);
        when(repository.save(any(Agent.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private Agent existingAgent(int version) {
        Agent agent = new Agent();
        agent.setId(1L);
        agent.setName("Old name");
        agent.setDescription("Old description");
        agent.setStatus(AgentStatus.DRAFT);
        agent.setModelProvider("old-provider");
        agent.setModelName("old-model");
        agent.setTemperature(0.7);
        agent.setMaxTokens(100);
        agent.setSystemPrompt("old prompt");
        agent.setVisibility(AgentVisibility.PRIVATE);
        agent.setVersion(version);
        return agent;
    }

    private UpdateAgentRequest request(int version, double temperature) {
        return new UpdateAgentRequest(
                version,
                new AgentDetailResponse.Information("New name", "New description", AgentStatus.ACTIVE),
                new AgentDetailResponse.Model("new-provider", "new-model", temperature, 500),
                "new prompt",
                AgentVisibility.PUBLIC);
    }

    @Test
    void updatesAllFieldsAndSavesAgent() {
        Agent agent = existingAgent(1);
        when(repository.findById(1L)).thenReturn(Optional.of(agent));

        Agent result = service.updateAgent(1L, request(1, 1.2));

        assertEquals("New name", result.getName());
        assertEquals("New description", result.getDescription());
        assertEquals(AgentStatus.ACTIVE, result.getStatus());
        assertEquals("new-provider", result.getModelProvider());
        assertEquals("new-model", result.getModelName());
        assertEquals(1.2, result.getTemperature());
        assertEquals(500, result.getMaxTokens());
        assertEquals("new prompt", result.getSystemPrompt());
        assertEquals(AgentVisibility.PUBLIC, result.getVisibility());
        verify(repository).save(agent);
    }

    @Test
    void acceptsTemperatureAtUpperBound() {
        when(repository.findById(1L)).thenReturn(Optional.of(existingAgent(1)));

        assertDoesNotThrow(() -> service.updateAgent(1L, request(1, 2.0)));
    }

    @Test
    void rejectsTemperatureAboveLimitWithoutSaving() {
        when(repository.findById(1L)).thenReturn(Optional.of(existingAgent(1)));

        InvalidAgentConfigException ex = assertThrows(InvalidAgentConfigException.class,
                () -> service.updateAgent(1L, request(1, 2.1)));

        assertEquals(InvalidAgentConfigException.TEMPERATURE_TOO_HIGH, ex.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void rejectsStaleVersionWithoutSaving() {
        when(repository.findById(1L)).thenReturn(Optional.of(existingAgent(5)));

        assertThrows(InvalidAgentConfigException.class, () -> service.updateAgent(1L, request(4, 0.5)));

        verify(repository, never()).save(any());
    }

    @Test
    void acceptsMatchingVersion() {
        when(repository.findById(1L)).thenReturn(Optional.of(existingAgent(5)));

        assertDoesNotThrow(() -> service.updateAgent(1L, request(5, 0.5)));
    }

    @Test
    void throwsNotFoundWhenAgentDoesNotExist() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(AgentNotFoundException.class, () -> service.updateAgent(99L, request(1, 0.5)));

        verify(repository, never()).save(any());
    }
}
