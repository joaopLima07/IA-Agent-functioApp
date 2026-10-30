package br.com.aisaas.conversation.domain.exceptions;

public class GeminiAPINotRespondException extends RuntimeException {
    public GeminiAPINotRespondException(String message) {
        super(message);
    }

    public GeminiAPINotRespondException(String message, Throwable cause) {
        super(message, cause);
    }
}
