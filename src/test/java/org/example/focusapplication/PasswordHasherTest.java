package org.example.focusapplication;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordHasherTest {
    @Test
    void hashProducesVerifiableValue() {
        String storedHash = PasswordHasher.hash("correct horse battery staple".toCharArray());

        assertTrue(storedHash.startsWith("PBKDF2$"));
        assertTrue(PasswordHasher.verify("correct horse battery staple".toCharArray(), storedHash));
    }

    @Test
    void verifyRejectsWrongPasswordAndInvalidInput() {
        String storedHash = PasswordHasher.hash("one-password".toCharArray());

        assertFalse(PasswordHasher.verify("other-password".toCharArray(), storedHash));
        assertFalse(PasswordHasher.verify("anything".toCharArray(), null));
        assertFalse(PasswordHasher.verify("anything".toCharArray(), ""));
        assertFalse(PasswordHasher.verify("anything".toCharArray(), "not-a-pbkdf2-hash"));
    }
}
