package br.com.aisaas.auth.application;

import br.com.aisaas.auth.api.dto.LoginRequest;
import br.com.aisaas.auth.domain.User;
import br.com.aisaas.auth.domain.UserStatus;
import br.com.aisaas.auth.domain.exceptions.InvalidCredentialsException;
import br.com.aisaas.auth.domain.exceptions.UserNotActiveException;
import br.com.aisaas.auth.infra.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoginServiceTest {

    private static final String PASSWORD = "Abcdef1!";

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    private UserRepository repository;
    private PasswordPolicy passwordPolicy;
    private JwtTokenService jwtTokenService;
    private RefreshTokenService refreshTokenService;
    private LoginService service;

    @BeforeEach
    void setUp() {
        repository = mock(UserRepository.class);
        passwordPolicy = spy(new PasswordPolicy(encoder));
        jwtTokenService = mock(JwtTokenService.class);
        refreshTokenService = mock(RefreshTokenService.class);
        service = new LoginService(repository, new Sanitizer(), passwordPolicy, jwtTokenService, refreshTokenService);
    }

    private User user(UserStatus status) {
        User user = new User();
        user.setId(7L);
        user.setEmail("joao@example.com");
        user.setPassword(encoder.encode(PASSWORD));
        user.setStatus(status);
        return user;
    }

    @Test
    void returnsTokensAndExpirationWhenCredentialsAreValid() {
        User user = user(UserStatus.ACTIVE);
        when(repository.findByEmail("joao@example.com")).thenReturn(Optional.of(user));
        when(jwtTokenService.generateToken(user)).thenReturn("jwt-token");
        when(jwtTokenService.getExpirationSeconds()).thenReturn(900L);
        when(refreshTokenService.issue(user)).thenReturn("refresh-token");

        AuthTokens tokens = service.login(new LoginRequest("  JOAO@Example.com ", PASSWORD));

        assertEquals("jwt-token", tokens.accessToken());
        assertEquals(900L, tokens.expiresIn());
        assertEquals("refresh-token", tokens.refreshToken());
    }

    @Test
    void rejectsUnknownEmailWithGenericMessageAndStillSpendsAPasswordCheck() {
        when(repository.findByEmail("joao@example.com")).thenReturn(Optional.empty());

        InvalidCredentialsException ex = assertThrows(InvalidCredentialsException.class,
                () -> service.login(new LoginRequest("joao@example.com", PASSWORD)));

        assertEquals("E-mail ou senha inválidos.", ex.getMessage());
        verify(passwordPolicy).burnPasswordCheck(PASSWORD);
        verifyNoInteractions(jwtTokenService);
    }

    @Test
    void rejectsWrongPasswordWithSameMessageAsUnknownEmail() {
        when(repository.findByEmail("joao@example.com")).thenReturn(Optional.of(user(UserStatus.ACTIVE)));

        InvalidCredentialsException ex = assertThrows(InvalidCredentialsException.class,
                () -> service.login(new LoginRequest("joao@example.com", "Wrong1234!")));

        assertEquals("E-mail ou senha inválidos.", ex.getMessage());
        verifyNoInteractions(jwtTokenService);
    }

    @Test
    void rejectsInactiveUserEvenWithCorrectPassword() {
        when(repository.findByEmail("joao@example.com")).thenReturn(Optional.of(user(UserStatus.INACTIVE)));

        assertThrows(UserNotActiveException.class,
                () -> service.login(new LoginRequest("joao@example.com", PASSWORD)));
        verifyNoInteractions(jwtTokenService);
    }

    @Test
    void rejectsSuspendedUserEvenWithCorrectPassword() {
        when(repository.findByEmail("joao@example.com")).thenReturn(Optional.of(user(UserStatus.SUSPENDED)));

        assertThrows(UserNotActiveException.class,
                () -> service.login(new LoginRequest("joao@example.com", PASSWORD)));
        verifyNoInteractions(jwtTokenService);
    }

    @Test
    void doesNotRevealInactiveStatusWhenPasswordIsWrong() {
        when(repository.findByEmail("joao@example.com")).thenReturn(Optional.of(user(UserStatus.SUSPENDED)));

        assertThrows(InvalidCredentialsException.class,
                () -> service.login(new LoginRequest("joao@example.com", "Wrong1234!")));
    }
}
