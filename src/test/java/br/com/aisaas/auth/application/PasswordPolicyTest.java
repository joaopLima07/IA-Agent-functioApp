package br.com.aisaas.auth.application;

import br.com.aisaas.auth.application.PasswordPolicy;
import br.com.aisaas.auth.domain.exceptions.WeakPasswordException;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class PasswordPolicyTest {

    private final PasswordPolicy policy = new PasswordPolicy();

    @Test
    void validPasswordReturnsBcryptHash() {
        String raw = "Abcdef1!";

        String hash = policy.validPassword(raw);

        assertNotEquals(raw, hash);
        assertTrue(new BCryptPasswordEncoder().matches(raw, hash));
    }

    @Test
    void rejectsPasswordShorterThanEightCharacters() {
        assertThrows(WeakPasswordException.class, () -> policy.validPassword("Ab1!xyz"));
    }

    @Test
    void acceptsPasswordWithExactlyEightCharacters() {
        assertDoesNotThrow(() -> policy.validPassword("Abcdef1!"));
    }

    @Test
    void rejectsPasswordWithoutLetters() {
        assertThrows(WeakPasswordException.class, () -> policy.validPassword("12345678!"));
    }

    @Test
    void rejectsPasswordWithoutNumbers() {
        assertThrows(WeakPasswordException.class, () -> policy.validPassword("Abcdefgh!"));
    }

    @Test
    void rejectsPasswordWithoutSpecialCharacters() {
        assertThrows(WeakPasswordException.class, () -> policy.validPassword("Abcdefg12"));
    }

    @Test
    void sameRawPasswordProducesDifferentHashes() {
        assertNotEquals(policy.validPassword("Abcdef1!"), policy.validPassword("Abcdef1!"));
    }
}
