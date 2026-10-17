package br.com.aisaas.agent.domain.exceptions;

public class InvalidAgentConfigException extends RuntimeException {
    public static final String TEMPERATURE_TOO_HIGH = "A temperatura é maior que o valor válido.";

    public InvalidAgentConfigException(String message) {
        super(message);
    }
}
