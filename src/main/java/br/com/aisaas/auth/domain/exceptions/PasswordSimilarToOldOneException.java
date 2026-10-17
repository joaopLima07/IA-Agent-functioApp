package br.com.aisaas.auth.domain.exceptions;

public class PasswordSimilarToOldOneException extends RuntimeException {
    public PasswordSimilarToOldOneException(String message) {
        super(message);
    }
}
