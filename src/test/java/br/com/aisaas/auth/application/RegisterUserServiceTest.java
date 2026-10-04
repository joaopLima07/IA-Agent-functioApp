package br.com.aisaas.auth.application;

import br.com.aisaas.auth.application.PasswordPolicy;
import br.com.aisaas.auth.application.RegisterUserCommand;
import br.com.aisaas.auth.application.RegisterUserService;
import br.com.aisaas.auth.application.Sanitizer;
import br.com.aisaas.auth.domain.User;
import br.com.aisaas.auth.domain.exceptions.EmailAlreadyRegisteredException;
import br.com.aisaas.auth.domain.exceptions.InvalidPhoneNumberException;
import br.com.aisaas.auth.domain.exceptions.WeakPasswordException;
import br.com.aisaas.auth.infra.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RegisterUserServiceTest {

    private UserRepository repository;
    private RegisterUserService service;

    @BeforeEach
    void setUp() {
        repository = mock(UserRepository.class);
        service = new RegisterUserService(repository, new PasswordPolicy(), new Sanitizer());
    }

    private RegisterUserCommand command() {
        return new RegisterUserCommand("  João Lima ", "JOAO@Example.com", "Abcdef1!", "(11) 98765-4321");
    }

    @Test
    void createsUserWithSanitizedDataAndHashedPassword() {
        when(repository.findByEmail("joao@example.com")).thenReturn(Optional.empty());
        when(repository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(7L);
            return u;
        });

        User response = service.createUser(command());

        assertEquals(7L, response.getId());
        assertEquals("João Lima", response.getFullName());
        assertEquals("joao@example.com", response.getEmail());

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(repository).save(saved.capture());
        assertEquals("+5511987654321", saved.getValue().getPhone());
        assertNotEquals("Abcdef1!", saved.getValue().getPassword());
        assertTrue(new BCryptPasswordEncoder().matches("Abcdef1!", saved.getValue().getPassword()));
    }

    @Test
    void rejectsEmailThatIsAlreadyRegistered() {
        when(repository.findByEmail("joao@example.com")).thenReturn(Optional.of(new User()));

        assertThrows(EmailAlreadyRegisteredException.class, () -> service.createUser(command()));

        verify(repository, never()).save(any());
    }

    @Test
    void rejectsInvalidPhoneWithoutSaving() {
        RegisterUserCommand cmd = new RegisterUserCommand("João", "joao@example.com", "Abcdef1!", "123");

        assertThrows(InvalidPhoneNumberException.class, () -> service.createUser(cmd));

        verify(repository, never()).save(any());
    }

    @Test
    void rejectsWeakPasswordWithoutSaving() {
        RegisterUserCommand cmd = new RegisterUserCommand("João", "joao@example.com", "abc", "(11) 98765-4321");

        assertThrows(WeakPasswordException.class, () -> service.createUser(cmd));

        verify(repository, never()).save(any());
    }
}
