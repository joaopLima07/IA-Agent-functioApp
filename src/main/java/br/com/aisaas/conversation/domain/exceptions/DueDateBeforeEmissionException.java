package br.com.aisaas.conversation.domain.exceptions;

public class DueDateBeforeEmissionException extends RuntimeException {
    public DueDateBeforeEmissionException(String message) {
        super(message);
    }
}
