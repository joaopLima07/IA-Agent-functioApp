package br.com.aisaas.conversation.domain.exceptions;

public class ValueZeradoOrLesserThanZeroException extends RuntimeException {
    public ValueZeradoOrLesserThanZeroException(String message) {
        super(message);
    }
}
