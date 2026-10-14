package br.com.aisaas.auth.api;

import br.com.aisaas.auth.domain.exceptions.*;
import br.com.aisaas.shared.api.dto.ErrorResponse;
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
                .orElse("Requisição inválida");
        return ResponseEntity.status(400).body(new ErrorResponse("VALIDATION_ERROR", message));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> invalidCredentials(InvalidCredentialsException e){
        return ResponseEntity.status(401).body(new ErrorResponse("INVALID_CREDENTIALS", e.getMessage()));
    }

    @ExceptionHandler(UserNotActiveException.class)
    public ResponseEntity<ErrorResponse> userNotActive(UserNotActiveException e){
        return ResponseEntity.status(403).body(new ErrorResponse("USER_NOT_ACTIVE", e.getMessage()));
    }

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    public ResponseEntity<ErrorResponse> emailAlreadyRegistered(EmailAlreadyRegisteredException e){
        return ResponseEntity.status(409).body(new ErrorResponse("EMAIL_ALREADY_REGISTERED", e.getMessage()));
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ErrorResponse> invalidRefreshToken(InvalidRefreshTokenException e){
        return ResponseEntity.status(401).body(new ErrorResponse("INVALID_REFRESH_TOKEN", e.getMessage()));
    }

    @ExceptionHandler(InvalidResetTokenException.class)
    public ResponseEntity<ErrorResponse> invalidResetTokenException(InvalidResetTokenException e){
        return ResponseEntity.status(400).body(new ErrorResponse("INVALID_RESET_TOKEN", e.getMessage()));
    }

    @ExceptionHandler(PasswordSimilarToOldOneException.class)
    public ResponseEntity<ErrorResponse> passwordSimiliarToOldOne(PasswordSimilarToOldOneException e){
        return ResponseEntity.status(422).body(new ErrorResponse("PASSWORD_SIMILIAR", e.getMessage()));
    }

}
