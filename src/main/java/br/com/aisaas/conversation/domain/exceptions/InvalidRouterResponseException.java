package br.com.aisaas.conversation.domain.exceptions;

public class InvalidRouterResponseException extends RuntimeException {
    public InvalidRouterResponseException(String message) {
        super(message);
    }
}
