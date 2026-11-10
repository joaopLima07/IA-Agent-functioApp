package br.com.aisaas.conversation.domain.exceptions;

public class TaxAmountHigherThanTotalAmount extends RuntimeException {
    public TaxAmountHigherThanTotalAmount(String message) {
        super(message);
    }
}
