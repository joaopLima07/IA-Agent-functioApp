package br.com.aisaas.conversation.domain.exceptions;

public class InvalidCNPJException extends RuntimeException {
    public InvalidCNPJException(String message) {
        super(message);
    }
}
