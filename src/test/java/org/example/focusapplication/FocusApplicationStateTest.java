package org.example.focusapplication;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FocusApplicationStateTest {
    @Test
    void loginStatusMessageCanBeStoredForNextLoginScene() throws Exception {
        FocusApplication application = new FocusApplication();
        Field field = FocusApplication.class.getDeclaredField("loginStatusMessage");
        field.setAccessible(true);
        field.set(application, "Account created successfully. Please sign in.");

        assertEquals("Account created successfully. Please sign in.", field.get(application));
    }

    @Test
    void consumeLoginStatusMessageClearsPendingValue() throws Exception {
        FocusApplication application = new FocusApplication();
        Field field = FocusApplication.class.getDeclaredField("loginStatusMessage");
        field.setAccessible(true);
        field.set(application, "Account created successfully. Please sign in.");

        assertEquals("Account created successfully. Please sign in.", application.consumeLoginStatusMessage());
        assertEquals(null, field.get(application));
    }
}
