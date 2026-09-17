package org.example.focusapplication;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class FocusApplication extends Application {
    private Stage primaryStage;
    private String currentUsername;
    private String loginStatusMessage;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        AuthHttpServer.startServer();
        primaryStage.setTitle("Focus Application");
        primaryStage.setScene(buildLoginScene());
        primaryStage.setMinWidth(420);
        primaryStage.setMinHeight(500);
        primaryStage.show();
    }

    public void showLoginScene() {
        primaryStage.setTitle("Focus Application");
        primaryStage.setScene(buildLoginScene());
    }

    public void showLoginScene(String statusMessage) {
        this.loginStatusMessage = statusMessage;
        showLoginScene();
    }

    public void showCreateAccountScene() {
        primaryStage.setTitle("Create Account");
        primaryStage.setScene(buildCreateAccountScene());
    }

    public void showLandingScene(String username) {
        this.currentUsername = username;
        primaryStage.setTitle("Focus Dashboard");
        primaryStage.setScene(buildLandingScene(username));
    }

    public String getCurrentUsername() {
        return currentUsername;
    }

    String consumeLoginStatusMessage() {
        String message = loginStatusMessage;
        loginStatusMessage = null;
        return message;
    }

    private Scene buildLoginScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #1f2937, #111827);");

        VBox form = new VBox(16);
        form.setPadding(new Insets(32, 36, 32, 36));
        form.setAlignment(javafx.geometry.Pos.CENTER);

        Label title = new Label("Focus Application");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label subtitle = new Label("Sign in to continue");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #d1d5db;");

        VBox fields = new VBox(10);
        fields.setMaxWidth(300);

        Label usernameLabel = new Label("Username");
        usernameLabel.setStyle("-fx-text-fill: white;");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username");

        Label passwordLabel = new Label("Password");
        passwordLabel.setStyle("-fx-text-fill: white;");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");

        fields.getChildren().addAll(usernameLabel, usernameField, passwordLabel, passwordField);

        Button loginButton = new Button("Log In");
        loginButton.setDefaultButton(true);
        loginButton.setMaxWidth(300);
        loginButton.setStyle("-fx-background-color: #8b5cf6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 16 10 16;");

        Label statusLabel = new Label();
        statusLabel.setWrapText(true);
        statusLabel.setStyle("-fx-text-fill: #fbbf24; -fx-font-size: 12px;");
        String pendingLoginStatus = consumeLoginStatusMessage();
        if (pendingLoginStatus != null && !pendingLoginStatus.isBlank()) {
            statusLabel.setText(pendingLoginStatus);
        }

        Hyperlink createAccountLink = new Hyperlink("Create an account");
        createAccountLink.setStyle("-fx-text-fill: #c4b5fd; -fx-font-size: 13px;");
        createAccountLink.setOnAction(event -> showCreateAccountScene());

        loginButton.setOnAction(event -> {
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
                showLandingScene(username);
            } catch (Exception e) {
                statusLabel.setText("Unable to reach the authentication service.");
            }
        });

        form.getChildren().addAll(title, subtitle, fields, loginButton, createAccountLink, statusLabel);
        root.setCenter(form);
        return new Scene(root, 420, 500);
    }

    private Scene buildCreateAccountScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #111827, #1f2937);");

        VBox form = new VBox(16);
        form.setPadding(new Insets(32, 36, 32, 36));
        form.setAlignment(javafx.geometry.Pos.CENTER);

        Label title = new Label("Create Account");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label subtitle = new Label("Set up your focus profile");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #d1d5db;");

        VBox fields = new VBox(10);
        fields.setMaxWidth(300);

        Label usernameLabel = new Label("Username");
        usernameLabel.setStyle("-fx-text-fill: white;");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Choose a username");

        Label passwordLabel = new Label("Password");
        passwordLabel.setStyle("-fx-text-fill: white;");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Choose a password");

        Label confirmPasswordLabel = new Label("Confirm Password");
        confirmPasswordLabel.setStyle("-fx-text-fill: white;");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm your password");

        fields.getChildren().addAll(usernameLabel, usernameField, passwordLabel, passwordField, confirmPasswordLabel, confirmPasswordField);

        Button createButton = new Button("Create Account");
        createButton.setDefaultButton(true);
        createButton.setMaxWidth(300);
        createButton.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 16 10 16;");

        Label statusLabel = new Label();
        statusLabel.setWrapText(true);
        statusLabel.setStyle("-fx-text-fill: #fbbf24; -fx-font-size: 12px;");

        Hyperlink backLink = new Hyperlink("Back to login");
        backLink.setStyle("-fx-text-fill: #bfdbfe; -fx-font-size: 13px;");
        backLink.setOnAction(event -> showLoginScene());

        createButton.setOnAction(event -> {
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
                ApiClient.ApiResult result = ApiClient.createAccount(username, password);
                if (!result.success()) {
                    statusLabel.setText(result.message());
                    return;
                }
                showLoginScene("Account created successfully. Please sign in.");
            } catch (Exception e) {
                statusLabel.setText("Unable to reach the backend service.");
            }
        });

        form.getChildren().addAll(title, subtitle, fields, createButton, backLink, statusLabel);
        root.setCenter(form);
        return new Scene(root, 420, 500);
    }

    private Scene buildLandingScene(String username) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #0f172a, #1e293b);");

        VBox content = new VBox(16);
        content.setPadding(new Insets(36));
        content.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label title = new Label("Welcome back, " + username + "!");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label subtitle = new Label("Your focus space is ready.");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #cbd5e1;");

        Label sectionLabel = new Label("Today");
        sectionLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e2e8f0;");

        TextArea notes = new TextArea("Start with one high-priority task.\nTake a short break every 25 minutes.\nKeep distractions out of view.");
        notes.setEditable(false);
        notes.setWrapText(true);
        notes.setPrefRowCount(6);
        notes.setStyle("-fx-control-inner-background: #111827; -fx-text-fill: white;");

        Button logoutButton = new Button("Log Out");
        logoutButton.setOnAction(event -> showLoginScene());
        logoutButton.setStyle("-fx-background-color: #334155; -fx-text-fill: white; -fx-font-weight: bold;");

        content.getChildren().addAll(title, subtitle, sectionLabel, notes, logoutButton);
        root.setCenter(content);
        return new Scene(root, 560, 420);
    }
}
