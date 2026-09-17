package org.example.focusapplication;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class FocusController {
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label statusLabel;

    private FocusApplication application;

    public void setApplication(FocusApplication application) {
        this.application = application;
    }

    @FXML
    protected void handleLogin() {
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter both a username and password.");
            return;
        }

        try {
            ApiClient.ApiResult result = ApiClient.login(username, password);
            if (!result.success()) {
                statusLabel.setText(result.message());
                return;
            }

            statusLabel.setText("Welcome, " + username + ". Your focus session is ready.");
        } catch (Exception e) {
            statusLabel.setText("Unable to reach the authentication service.");
        }
    }

    @FXML
    protected void onCreateAccountClicked() {
        if (application != null) {
            application.showCreateAccountScene();
        }
    }
}
