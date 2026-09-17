package org.example.focusapplication;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CreateAccountControllerTest {
    @Test
    void accountCreationServiceProvidesRedirectMessage() throws Exception {
        CreateAccountController.AccountCreationService service = (username, password) ->
                new ApiClient.ApiResult(true, "Account created successfully.");

        ApiClient.ApiResult result = service.createAccount("tester", "secret");

        assertEquals(true, result.success());
        assertEquals("Account created successfully.", result.message());
    }
}
