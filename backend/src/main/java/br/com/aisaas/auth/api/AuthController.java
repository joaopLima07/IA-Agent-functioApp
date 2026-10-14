package br.com.aisaas.auth.api;

import br.com.aisaas.auth.api.dto.*;
import br.com.aisaas.auth.application.*;
import br.com.aisaas.auth.domain.User;
import br.com.aisaas.auth.domain.UserStatus;
import br.com.aisaas.auth.domain.exceptions.InvalidCredentialsException;
import br.com.aisaas.auth.domain.exceptions.InvalidRefreshTokenException;
import br.com.aisaas.auth.domain.exceptions.UserNotActiveException;
import br.com.aisaas.auth.infra.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthController {
    public final RegisterUserService registerUserService;
    private final LoginService loginService;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final RefreshCookieFactory cookieFactory;
    public final PasswordResetService passwordResetService;

    public AuthController(RegisterUserService registerUserService, LoginService loginService, UserRepository userRepository,
                          RefreshTokenService refreshTokenService, RefreshCookieFactory cookieFactory, PasswordResetService passwordResetService) {
        this.registerUserService = registerUserService;
        this.loginService = loginService;
        this.userRepository = userRepository;
        this.refreshTokenService = refreshTokenService;
        this.cookieFactory = cookieFactory;
        this.passwordResetService = passwordResetService;
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

    @PostMapping("/api/auth/forgotPassword")
    public ResponseEntity<Object> forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest){
        passwordResetService.generatedToken(forgotPasswordRequest);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/auth/verifyResetCode")
    public ResponseEntity<VerifyResetCodeResponse> verifyResetCode(@Valid @RequestBody VerifyResetCodeRequest request){
        return ResponseEntity.ok(passwordResetService.verifyResetCode(request));
    }

    @PostMapping("/api/auth/resetPassword")
    public ResponseEntity<Object> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest){
        passwordResetService.resetPassword(resetPasswordRequest);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/auth/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request){
        return withRefreshCookie(loginService.login(request));
    }

    @GetMapping("/api/auth/me")
    public ResponseEntity<MeResponse> me(@AuthenticationPrincipal Jwt jwt){
        long userId = Long.parseLong(jwt.getSubject());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidCredentialsException("Usuário não encontrado"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UserNotActiveException("Usuário não está ativo");
        }

        return ResponseEntity.ok(new MeResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole().name()
        ));
    }

    @PostMapping("/api/auth/refresh")
    public ResponseEntity<LoginResponse> refresh(@CookieValue(name = RefreshCookieFactory.COOKIE_NAME, required = false) String refreshToken){
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new InvalidRefreshTokenException("Token de atualização ausente.");
        }
        return withRefreshCookie(refreshTokenService.rotate(refreshToken));
    }

    @PostMapping("/api/auth/logout")
    public ResponseEntity<Void> logout(@CookieValue(name = RefreshCookieFactory.COOKIE_NAME, required = false) String refreshToken){
        refreshTokenService.revoke(refreshToken);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookieFactory.clear().toString())
                .build();
    }

    private ResponseEntity<LoginResponse> withRefreshCookie(AuthTokens tokens){
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieFactory.create(tokens.refreshToken()).toString())
                .body(new LoginResponse(tokens.accessToken(), tokens.expiresIn()));
    }
}
