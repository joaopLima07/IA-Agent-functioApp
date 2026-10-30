package br.com.aisaas.agent.api;

import br.com.aisaas.agent.application.AgentQueryService;
import br.com.aisaas.agent.api.dto.UpdateAgentRequest;
import br.com.aisaas.agent.domain.Agent;
import br.com.aisaas.agent.domain.AgentStatus;
import br.com.aisaas.agent.domain.AgentVisibility;
import br.com.aisaas.agent.domain.exceptions.AgentNotFoundException;
import br.com.aisaas.agent.domain.exceptions.InvalidAgentConfigException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AgentControllerTest {

    private static final String VALID_BODY = """
            {"version":1,
             "information":{"name":"New name","description":"New description","status":"ACTIVE"},
             "model":{"modelProvider":"new-provider","modelName":"new-model","temperature":1.0,"maxTokens":500},
             "systemPrompt":"new prompt",
             "visibility":"PUBLIC"}
            """;

    private AgentQueryService service;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        service = mock(AgentQueryService.class);
        mvc = MockMvcBuilders.standaloneSetup(new AgentController(service))
                .setControllerAdvice(new AgentControllerAdvice())
                .build();
    }

    private ResultActions update(long id, String body) throws Exception {
        return mvc.perform(put("/api/agents/" + id).contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private Agent updatedAgent() {
        Agent agent = new Agent();
        agent.setId(1L);
        agent.setName("New name");
        agent.setDescription("New description");
        agent.setStatus(AgentStatus.ACTIVE);
        agent.setModelProvider("new-provider");
        agent.setModelName("new-model");
        agent.setTemperature(1.0);
        agent.setMaxTokens(500);
        agent.setSystemPrompt("new prompt");
        agent.setVisibility(AgentVisibility.PUBLIC);
        agent.setVersion(2);
        return agent;
    }

    @Test
    void returns200WithUpdatedAgent() throws Exception {
        when(service.updateAgent(eq(1L), any(UpdateAgentRequest.class))).thenReturn(updatedAgent());

        update(1, VALID_BODY)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.version").value(2))
                .andExpect(jsonPath("$.information.name").value("New name"))
                .andExpect(jsonPath("$.information.status").value("ACTIVE"))
                .andExpect(jsonPath("$.model.modelName").value("new-model"))
                .andExpect(jsonPath("$.model.temperature").value(1.0))
                .andExpect(jsonPath("$.systemPrompt").value("new prompt"))
                .andExpect(jsonPath("$.visibility").value("PUBLIC"));
    }

    @Test
    void passesPathIdToService() throws Exception {
        when(service.updateAgent(eq(7L), any(UpdateAgentRequest.class))).thenReturn(updatedAgent());

        update(7, VALID_BODY).andExpect(status().isOk());

        verify(service).updateAgent(eq(7L), any(UpdateAgentRequest.class));
    }

    @Test
    void returns404WhenAgentNotFound() throws Exception {
        when(service.updateAgent(eq(99L), any())).thenThrow(new AgentNotFoundException("Not found agent with id 99"));

        update(99, VALID_BODY).andExpect(status().isNotFound());
    }

    @Test
    void returns422WhenTemperatureIsAboveLimit() throws Exception {
        when(service.updateAgent(eq(1L), any()))
                .thenThrow(new InvalidAgentConfigException(InvalidAgentConfigException.TEMPERATURE_TOO_HIGH));

        update(1, VALID_BODY).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void returns409WhenVersionIsStale() throws Exception {
        when(service.updateAgent(eq(1L), any()))
                .thenThrow(new InvalidAgentConfigException("This agent already modificated by another person, reload."));

        update(1, VALID_BODY).andExpect(status().isConflict());
    }

    @Test
    void returns409WhenConcurrentUpdateTriggersOptimisticLock() throws Exception {
        when(service.updateAgent(eq(1L), any()))
                .thenThrow(new ObjectOptimisticLockingFailureException(Agent.class, 1L));

        update(1, VALID_BODY).andExpect(status().isConflict());
    }

    @Test
    void returns400WhenVersionIsMissing() throws Exception {
        update(1, VALID_BODY.replace("\"version\":1,", "")).andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void returns400WhenInformationIsMissing() throws Exception {
        String body = """
                {"version":1,
                 "model":{"modelProvider":"p","modelName":"m","temperature":1.0,"maxTokens":500},
                 "systemPrompt":"prompt","visibility":"PUBLIC"}
                """;

        update(1, body).andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void returns400WhenSystemPromptIsBlank() throws Exception {
        update(1, VALID_BODY.replace("\"new prompt\"", "\"  \"")).andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void returns400WhenVisibilityIsMissing() throws Exception {
        update(1, VALID_BODY.replace(",\n \"visibility\":\"PUBLIC\"", "")).andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }
}
