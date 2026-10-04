package br.com.aisaas.auth.api;

import br.com.aisaas.auth.application.RegisterUserCommand;
import br.com.aisaas.auth.application.RegisterUserService;
import br.com.aisaas.auth.domain.User;
import br.com.aisaas.auth.domain.exceptions.EmailAlreadyRegisteredException;
import br.com.aisaas.auth.domain.exceptions.InvalidPhoneNumberException;
import br.com.aisaas.auth.domain.exceptions.WeakPasswordException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest {

    private static final String VALID_BODY = """
            {"fullName":"João Lima","email":"joao@example.com","password":"Abcdef1!","phone":"(11) 98765-4321"}
            """;

    private RegisterUserService service;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        service = mock(RegisterUserService.class);
        mvc = MockMvcBuilders.standaloneSetup(new AuthController(service)).build();
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
    void returns400WhenPasswordIsWeak() throws Exception {
        when(service.createUser(any())).thenThrow(new WeakPasswordException("weak"));

        register(VALID_BODY).andExpect(status().isBadRequest()).andExpect(content().string("weak"));
    }

    @Test
    void returns400WhenPhoneIsInvalid() throws Exception {
        when(service.createUser(any())).thenThrow(new InvalidPhoneNumberException("bad phone"));

        register(VALID_BODY).andExpect(status().isBadRequest()).andExpect(content().string("bad phone"));
    }

    @Test
    void returns400WhenEmailAlreadyRegistered() throws Exception {
        when(service.createUser(any())).thenThrow(new EmailAlreadyRegisteredException("taken"));

        register(VALID_BODY).andExpect(status().isBadRequest()).andExpect(content().string("taken"));
    }

    @Test
    void returns400WhenEmailFormatIsInvalid() throws Exception {
        register("""
                {"fullName":"João","email":"not-an-email","password":"Abcdef1!","phone":"(11) 98765-4321"}
                """).andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void returns400WhenRequiredFieldIsBlank() throws Exception {
        register("""
                {"fullName":"  ","email":"joao@example.com","password":"Abcdef1!","phone":"(11) 98765-4321"}
                """).andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }
}
