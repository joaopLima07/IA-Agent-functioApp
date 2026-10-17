package br.com.aisaas.auth.application;

import br.com.aisaas.auth.application.Sanitizer;
import br.com.aisaas.auth.domain.exceptions.InvalidPhoneNumberException;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SanitizeTest {

    private final Sanitizer sanitize = new Sanitizer();

    @Test
    void fullNameIsTrimmed() {
        assertEquals("João Lima", sanitize.sanitizeFullName("  João Lima  "));
    }

    @Test
    void emailIsLowercased() {
        assertEquals("admin@example.com", sanitize.sanitizeEmail("ADMIN@Example.COM"));
    }

    @Test
    void emailIsTrimmed() {
        assertEquals("admin@example.com", sanitize.sanitizeEmail("  admin@example.com  "));
    }

    @Test
    void validBrazilianMobileIsFormattedAsE164() {
        assertEquals(Optional.of("+5511987654321"), sanitize.sanitizePhoneNumber("(11) 98765-4321"));
    }

    @Test
    void internationalNumberIsKeptAsE164() {
        assertEquals(Optional.of("+5511987654321"), sanitize.sanitizePhoneNumber("+55 11 98765-4321"));
    }

    @Test
    void parsableButInvalidNumberReturnsEmpty() {
        assertTrue(sanitize.sanitizePhoneNumber("123").isEmpty());
    }

    @Test
    void unparsableNumberThrowsInvalidPhoneNumber() {
        assertThrows(InvalidPhoneNumberException.class, () -> sanitize.sanitizePhoneNumber("abc"));
    }
}
