package br.com.aisaas.conversation.domain.exceptions;

public class InvalidMessageException extends RuntimeException {
    public InvalidMessageException(String message) {
        super(message);
    }
}
