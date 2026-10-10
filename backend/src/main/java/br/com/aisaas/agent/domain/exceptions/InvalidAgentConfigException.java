package br.com.aisaas.agent.domain.exceptions;

public class InvalidAgentConfigException extends RuntimeException {
    public InvalidAgentConfigException(String message) {
        super(message);
    }
}
