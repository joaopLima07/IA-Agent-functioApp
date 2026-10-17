package br.com.aisaas.agent.domain.exceptions;

public class AgentNotFoundException extends RuntimeException {
    public AgentNotFoundException(String message) {
        super(message);
    }
}
