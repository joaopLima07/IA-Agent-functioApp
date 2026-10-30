package br.com.aisaas.conversation.domain.exceptions;

public class MediaTypeNotSuportedException extends RuntimeException {
    public MediaTypeNotSuportedException(String message) {
        super(message);
    }
}
