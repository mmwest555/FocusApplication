package org.example.focusapplication;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class CreateAccountController {
    interface AccountCreationService {
        ApiClient.ApiResult createAccount(String username, String password) throws Exception;
    }

    private final AccountCreationService accountCreationService = ApiClient::createAccount;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label statusLabel;

    private FocusApplication application;

    public void setApplication(FocusApplication application) {
        this.application = application;
    }

    @FXML
    protected void handleCreateAccount() {
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText();
        String confirmPassword = confirmPasswordField.getText() == null ? "" : confirmPasswordField.getText();

        if (username.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            statusLabel.setText("Please fill out all fields.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            statusLabel.setText("Passwords do not match.");
            return;
        }

        try {
            ApiClient.ApiResult result = accountCreationService.createAccount(username, password);
            if (!result.success()) {
                statusLabel.setText(result.message());
                return;
            }

            if (application != null) {
                application.showLoginScene("Account created successfully. Please sign in.");
            }
        } catch (Exception e) {
            statusLabel.setText("Unable to reach the backend service.");
        }
    }

    @FXML
    protected void onBackToLoginClicked() {
        if (application != null) {
            application.showLoginScene();
        }
    }
}
