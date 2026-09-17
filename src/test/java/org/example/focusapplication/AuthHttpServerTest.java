package org.example.focusapplication;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class AuthHttpServerTest {
    @Test
    void extractsEscapedJsonFields() throws Exception {
        Method method = AuthHttpServer.class.getDeclaredMethod("extractField", String.class, java.util.regex.Pattern.class);
        method.setAccessible(true);

        String payload = "{\"username\":\"ann\\\"e\",\"password\":\"pa\\\\ss\"}";
        String username = (String) method.invoke(null, payload, java.util.regex.Pattern.compile("\"username\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\""));
        String password = (String) method.invoke(null, payload, java.util.regex.Pattern.compile("\"password\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\""));

        assertEquals("ann\"e", username);
        assertEquals("pa\\ss", password);
    }

    @Test
    void escapeJsonEscapesBackslashesAndQuotes() throws Exception {
        Method method = AuthHttpServer.class.getDeclaredMethod("escapeJson", String.class);
        method.setAccessible(true);

        String escaped = (String) method.invoke(null, "a\\b\"c");

        assertEquals("a\\\\b\\\"c", escaped);
    }
}
