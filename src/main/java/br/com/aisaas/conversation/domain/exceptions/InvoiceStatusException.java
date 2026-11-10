package br.com.aisaas.conversation.domain.exceptions;

public class InvoiceStatusException extends RuntimeException {
    public InvoiceStatusException(String message) {
        super(message);
    }
}
