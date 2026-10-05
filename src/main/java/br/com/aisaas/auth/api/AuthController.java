package br.com.aisaas.auth.api;

import br.com.aisaas.auth.api.dto.LoginRequest;
import br.com.aisaas.auth.api.dto.LoginResponse;
import br.com.aisaas.auth.application.LoginService;
import br.com.aisaas.auth.application.RegisterUserCommand;
import br.com.aisaas.auth.application.RegisterUserService;
import br.com.aisaas.auth.api.dto.RegisterRequest;
import br.com.aisaas.auth.api.dto.RegisterResponse;
import br.com.aisaas.auth.domain.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
    public final RegisterUserService registerUserService;
    private final LoginService loginService;

    public AuthController(RegisterUserService registerUserService, LoginService loginService) {
        this.registerUserService = registerUserService;
        this.loginService = loginService;
    }

    @PostMapping("/api/auth/register")
    public ResponseEntity<RegisterResponse> createUser(@Valid @RequestBody RegisterRequest recordRequest){

        RegisterUserCommand user = new RegisterUserCommand(
                recordRequest.fullName(),
                recordRequest.email(),
                recordRequest.password(),
                recordRequest.phone()
        );
            User saved = registerUserService.createUser(user);
            RegisterResponse response = new RegisterResponse(saved.getId(), saved.getFullName(), saved.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/api/auth/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request){
        return ResponseEntity.ok(loginService.login(request));
    }
}
