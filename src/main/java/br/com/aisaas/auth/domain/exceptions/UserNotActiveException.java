package br.com.aisaas.auth.domain.exceptions;

public class UserNotActiveException extends RuntimeException {
    public UserNotActiveException(String message) {
        super(message);
    }
}
