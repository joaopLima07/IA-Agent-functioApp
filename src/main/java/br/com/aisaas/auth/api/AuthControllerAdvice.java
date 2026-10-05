package br.com.aisaas.auth.api;

import br.com.aisaas.auth.api.dto.ErrorResponse;
import br.com.aisaas.auth.domain.exceptions.EmailAlreadyRegisteredException;
import br.com.aisaas.auth.domain.exceptions.InvalidCredentialsException;
import br.com.aisaas.auth.domain.exceptions.InvalidPhoneNumberException;
import br.com.aisaas.auth.domain.exceptions.WeakPasswordException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = AuthController.class)
public class AuthControllerAdvice {

    @ExceptionHandler(InvalidPhoneNumberException.class)
    public ResponseEntity<ErrorResponse> invalidPhoneNumber(InvalidPhoneNumberException e){
        return ResponseEntity.status(422).body(new ErrorResponse("INVALID_PHONE", e.getMessage()));
    }

    @ExceptionHandler(WeakPasswordException.class)
    public ResponseEntity<ErrorResponse> weakPasswordError(WeakPasswordException e){
        return ResponseEntity.status(422).body(new ErrorResponse("WEAK_PASSWORD", e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validationError(MethodArgumentNotValidException e){
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Invalid request");
        return ResponseEntity.status(400).body(new ErrorResponse("VALIDATION_ERROR", message));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> invalidCredentials(InvalidCredentialsException e){
        return ResponseEntity.status(401).body(new ErrorResponse("INVALID_CREDENTIALS", e.getMessage()));
    }

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    public ResponseEntity<ErrorResponse> emailAlreadyRegistered(EmailAlreadyRegisteredException e){
        return ResponseEntity.status(409).body(new ErrorResponse("EMAIL_ALREADY_REGISTERED", e.getMessage()));
    }
}
