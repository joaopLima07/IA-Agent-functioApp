package br.com.aisaas.auth.api;

import br.com.aisaas.auth.application.AuthTokens;
import br.com.aisaas.auth.application.LoginService;
import br.com.aisaas.auth.application.RefreshTokenService;
import br.com.aisaas.auth.application.RegisterUserCommand;
import br.com.aisaas.auth.application.RegisterUserService;
import br.com.aisaas.auth.domain.User;
import br.com.aisaas.auth.domain.exceptions.EmailAlreadyRegisteredException;
import br.com.aisaas.auth.domain.exceptions.InvalidCredentialsException;
import br.com.aisaas.auth.domain.exceptions.InvalidPhoneNumberException;
import br.com.aisaas.auth.domain.exceptions.UserNotActiveException;
import br.com.aisaas.auth.domain.exceptions.WeakPasswordException;
import br.com.aisaas.auth.infra.RefreshTokenProperties;
import br.com.aisaas.auth.infra.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest {

    private static final String VALID_BODY = """
            {"fullName":"João Lima","email":"joao@example.com","password":"Abcdef1!","phone":"(11) 98765-4321"}
            """;

    private static final String LOGIN_BODY = """
            {"email":"joao@example.com","password":"Abcdef1!"}
            """;

    private RegisterUserService service;
    private LoginService loginService;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        service = mock(RegisterUserService.class);
        loginService = mock(LoginService.class);
        RefreshCookieFactory cookieFactory = new RefreshCookieFactory(
                new RefreshTokenProperties(Duration.ofDays(7), true, "Lax", "/api/auth"));
        mvc = MockMvcBuilders.standaloneSetup(new AuthController(
                        service, loginService, mock(UserRepository.class), mock(RefreshTokenService.class), cookieFactory,
                        mock(br.com.aisaas.auth.application.PasswordResetService.class)))
                .setControllerAdvice(new AuthControllerAdvice())
                .build();
    }

    private org.springframework.test.web.servlet.ResultActions register(String body) throws Exception {
        return mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body));
    }

    @Test
    void returns201WithCreatedUser() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setFullName("João Lima");
        user.setEmail("joao@example.com");
        user.setPassword("hash");
        when(service.createUser(any(RegisterUserCommand.class))).thenReturn(user);

        register(VALID_BODY)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullName").value("João Lima"))
                .andExpect(jsonPath("$.email").value("joao@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void returns422WhenPasswordIsWeak() throws Exception {
        when(service.createUser(any())).thenThrow(new WeakPasswordException("weak"));

        register(VALID_BODY)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("WEAK_PASSWORD"))
                .andExpect(jsonPath("$.message").value("weak"));
    }

    @Test
    void returns422WhenPhoneIsInvalid() throws Exception {
        when(service.createUser(any())).thenThrow(new InvalidPhoneNumberException("bad phone"));

        register(VALID_BODY)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("INVALID_PHONE"))
                .andExpect(jsonPath("$.message").value("bad phone"));
    }

    @Test
    void returns409WhenEmailAlreadyRegistered() throws Exception {
        when(service.createUser(any())).thenThrow(new EmailAlreadyRegisteredException("taken"));

        register(VALID_BODY)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_REGISTERED"))
                .andExpect(jsonPath("$.message").value("taken"));
    }

    @Test
    void returns400WhenEmailFormatIsInvalid() throws Exception {
        register("""
                {"fullName":"João","email":"not-an-email","password":"Abcdef1!","phone":"(11) 98765-4321"}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verifyNoInteractions(service);
    }

    @Test
    void returns400WhenRequiredFieldIsBlank() throws Exception {
        register("""
                {"fullName":"  ","email":"joao@example.com","password":"Abcdef1!","phone":"(11) 98765-4321"}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verifyNoInteractions(service);
    }

    private org.springframework.test.web.servlet.ResultActions login(String body) throws Exception {
        return mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body));
    }

    @Test
    void loginReturns200WithTokenAndExpiration() throws Exception {
        when(loginService.login(any())).thenReturn(new AuthTokens("jwt-token", 900, "refresh-token"));

        login(LOGIN_BODY)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt-token"))
                .andExpect(jsonPath("$.expiresIn").value(900))
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andExpect(header().string(HttpHeaders.SET_COOKIE,
                        org.hamcrest.Matchers.allOf(
                                org.hamcrest.Matchers.containsString("refresh_token=refresh-token"),
                                org.hamcrest.Matchers.containsString("HttpOnly"))));
    }

    @Test
    void loginReturns401WhenCredentialsAreInvalid() throws Exception {
        when(loginService.login(any())).thenThrow(new InvalidCredentialsException("Invalid e-mail or password."));

        login(LOGIN_BODY)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.message").value("Invalid e-mail or password."));
    }

    @Test
    void loginReturns403WhenUserIsNotActive() throws Exception {
        when(loginService.login(any())).thenThrow(new UserNotActiveException("This account is not active."));

        login(LOGIN_BODY)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("USER_NOT_ACTIVE"));
    }

    @Test
    void loginReturns400WhenEmailFormatIsInvalid() throws Exception {
        login("""
                {"email":"not-an-email","password":"Abcdef1!"}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verifyNoInteractions(loginService);
    }

    @Test
    void loginReturns400WhenPasswordIsBlank() throws Exception {
        login("""
                {"email":"joao@example.com","password":" "}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verifyNoInteractions(loginService);
    }
}
