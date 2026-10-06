package br.com.aisaas.auth.api;

import br.com.aisaas.auth.api.dto.*;
import br.com.aisaas.auth.application.LoginService;
import br.com.aisaas.auth.application.RegisterUserCommand;
import br.com.aisaas.auth.application.RegisterUserService;
import br.com.aisaas.auth.domain.User;
import br.com.aisaas.auth.domain.UserStatus;
import br.com.aisaas.auth.domain.exceptions.InvalidCredentialsException;
import br.com.aisaas.auth.domain.exceptions.UserNotActiveException;
import br.com.aisaas.auth.infra.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
    public final RegisterUserService registerUserService;
    private final LoginService loginService;
    private final UserRepository userRepository;

    public AuthController(RegisterUserService registerUserService, LoginService loginService, UserRepository userRepository) {
        this.registerUserService = registerUserService;
        this.loginService = loginService;
        this.userRepository = userRepository;
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

    @GetMapping("/api/auth/me")
    public ResponseEntity<MeResponse> me(@AuthenticationPrincipal Jwt jwt){
        long userId = Long.parseLong(jwt.getSubject());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UserNotActiveException("User is not active");
        }

        return ResponseEntity.ok(new MeResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole().name()
        ));
    }
}
