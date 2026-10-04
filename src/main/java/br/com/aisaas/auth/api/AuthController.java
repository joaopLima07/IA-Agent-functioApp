package br.com.aisaas.auth.api;

import br.com.aisaas.auth.application.RegisterUserCommand;
import br.com.aisaas.auth.application.RegisterUserService;
import br.com.aisaas.auth.api.dto.RegisterRequest;
import br.com.aisaas.auth.api.dto.RegisterResponse;
import br.com.aisaas.auth.domain.User;
import br.com.aisaas.auth.domain.exceptions.EmailAlreadyRegisteredException;
import br.com.aisaas.auth.domain.exceptions.InvalidPhoneNumberException;
import br.com.aisaas.auth.domain.exceptions.WeakPasswordException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
    public final RegisterUserService registerUserService;

    public AuthController(RegisterUserService registerUserService) {
        this.registerUserService = registerUserService;
    }

    @PostMapping("/api/auth/register")
    public ResponseEntity<Object> createUser(@Valid @RequestBody RegisterRequest recordRequest){

        RegisterUserCommand user = new RegisterUserCommand(
                recordRequest.fullName(),
                recordRequest.email(),
                recordRequest.password(),
                recordRequest.phone()
        );

        try {
            User saved = registerUserService.createUser(user);
            RegisterResponse response = new RegisterResponse(saved.getId(), saved.getFullName(), saved.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (InvalidPhoneNumberException | WeakPasswordException | EmailAlreadyRegisteredException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
