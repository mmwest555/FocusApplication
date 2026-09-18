package org.example.focusapplication;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;

public class FocusApplication extends Application {
    private static final DateTimeFormatter TASK_TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a");
    private Stage primaryStage;
    private String currentUsername;
    private String loginStatusMessage;
    private final ObservableList<FocusTask> tasks = FXCollections.observableArrayList();

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

    ObservableList<FocusTask> getTasks() {
        return tasks;
    }

    void saveTask(FocusTask taskToUpdate, String title, LocalTime startTime, LocalTime endTime, String taskType) {
        String normalizedTitle = title == null ? "" : title.trim();
        String normalizedTaskType = taskType == null ? "" : taskType.trim();

        if (taskToUpdate == null) {
            tasks.add(new FocusTask(normalizedTitle, startTime, endTime, normalizedTaskType));
        } else {
            int existingIndex = tasks.indexOf(taskToUpdate);
            if (existingIndex >= 0) {
                tasks.set(existingIndex, new FocusTask(normalizedTitle, startTime, endTime, normalizedTaskType));
            }
        }

        sortTasks();
    }

    void deleteTask(FocusTask task) {
        tasks.remove(task);
    }

    private void sortTasks() {
        FXCollections.sort(tasks, Comparator.comparing(FocusTask::getStartTime).thenComparing(FocusTask::getEndTime));
    }

    private ObservableList<String> buildTimeOptions() {
        ObservableList<String> options = FXCollections.observableArrayList();
        LocalTime time = LocalTime.MIDNIGHT;
        while (!time.equals(LocalTime.MIDNIGHT.minusMinutes(30))) {
            options.add(formatTime(time));
            time = time.plusMinutes(30);
            if (time.equals(LocalTime.MIDNIGHT)) {
                break;
            }
        }
        return options;
    }

    private String formatTime(LocalTime time) {
        return time.format(TASK_TIME_FORMATTER).toLowerCase();
    }

    private String taskTypeAccentColor(String taskType) {
        return switch (taskType) {
            case "Work" -> "#60a5fa";
            case "Personal" -> "#f472b6";
            case "Study" -> "#f59e0b";
            case "Health" -> "#34d399";
            case "Errand" -> "#f87171";
            default -> "#a78bfa";
        };
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

        VBox content = new VBox(18);
        content.setPadding(new Insets(36));
        content.setAlignment(Pos.TOP_LEFT);

        Label title = new Label("Welcome back, " + username + "!");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label subtitle = new Label("Create, organize, and manage your tasks by time frame.");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #cbd5e1;");

        Label sectionLabel = new Label("Task Manager");
        sectionLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e2e8f0;");

        VBox form = new VBox(10);
        form.setMaxWidth(420);

        Label formTitle = new Label("New Task");
        formTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");

        TextField taskTitleField = new TextField();
        taskTitleField.setPromptText("Task title");

        ComboBox<String> startTimeBox = new ComboBox<>(buildTimeOptions());
        startTimeBox.setPromptText("Start time");
        startTimeBox.setMaxWidth(Double.MAX_VALUE);

        ComboBox<String> endTimeBox = new ComboBox<>(buildTimeOptions());
        endTimeBox.setPromptText("End time");
        endTimeBox.setMaxWidth(Double.MAX_VALUE);

        ComboBox<String> taskTypeBox = new ComboBox<>();
        taskTypeBox.getItems().addAll("Work", "Personal", "Study", "Health", "Errand", "Other");
        taskTypeBox.setPromptText("Select a task type");
        taskTypeBox.setMaxWidth(Double.MAX_VALUE);

        Label formStatusLabel = new Label();
        formStatusLabel.setWrapText(true);
        formStatusLabel.setStyle("-fx-text-fill: #fbbf24; -fx-font-size: 12px;");

        Button saveTaskButton = new Button("Create Task");
        saveTaskButton.setStyle("-fx-background-color: #8b5cf6; -fx-text-fill: white; -fx-font-weight: bold;");

        final FocusTask[] editingTask = new FocusTask[1];

        saveTaskButton.setOnAction(event -> {
            String taskTitle = taskTitleField.getText() == null ? "" : taskTitleField.getText().trim();
            String startTimeValue = startTimeBox.getValue();
            String endTimeValue = endTimeBox.getValue();
            String taskType = taskTypeBox.getValue();

            if (taskTitle.isBlank() || startTimeValue == null || endTimeValue == null || taskType == null || taskType.isBlank()) {
                formStatusLabel.setText("Please provide a title, time range, and task type.");
                return;
            }

            LocalTime startTime = LocalTime.parse(startTimeValue.toUpperCase(), TASK_TIME_FORMATTER);
            LocalTime endTime = LocalTime.parse(endTimeValue.toUpperCase(), TASK_TIME_FORMATTER);
            if (!endTime.isAfter(startTime)) {
                formStatusLabel.setText("End time must be after the start time.");
                return;
            }

            saveTask(editingTask[0], taskTitle, startTime, endTime, taskType);

            boolean wasEditing = editingTask[0] != null;
            editingTask[0] = null;
            taskTitleField.clear();
            startTimeBox.setValue(null);
            endTimeBox.setValue(null);
            taskTypeBox.setValue(null);
            formTitle.setText("New Task");
            saveTaskButton.setText("Create Task");
            formStatusLabel.setText(wasEditing ? "Task updated." : "Task created.");
        });

        HBox timeRangeRow = new HBox(10, startTimeBox, endTimeBox);
        form.getChildren().addAll(formTitle, taskTitleField, timeRangeRow, taskTypeBox, saveTaskButton, formStatusLabel);

        ListView<FocusTask> taskListView = new ListView<>(tasks);
        taskListView.setPlaceholder(new Label("No tasks yet. Create one to get started."));
        taskListView.setPrefHeight(260);
        taskListView.setStyle("-fx-control-inner-background: #111827;");
        taskListView.setFocusTraversable(false);
        taskListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(FocusTask task, boolean empty) {
                super.updateItem(task, empty);

                if (empty || task == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                Label taskTitleLabel = new Label(task.getTitle());
                taskTitleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");

                Label taskMetaLabel = new Label(formatTime(task.getStartTime()) + " - " + formatTime(task.getEndTime()) + "  •  " + task.getTaskType());
                taskMetaLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #cbd5e1;");

                String accentColor = taskTypeAccentColor(task.getTaskType());
                Label taskTypePill = new Label(task.getTaskType());
                taskTypePill.setStyle("-fx-background-color: " + accentColor + "; -fx-background-radius: 999; -fx-padding: 4 10 4 10; -fx-text-fill: #0f172a; -fx-font-size: 11px; -fx-font-weight: bold;");

                VBox taskInfo = new VBox(4, taskTitleLabel, taskMetaLabel, taskTypePill);

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Button editButton = new Button("Edit");
                editButton.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white;");
                editButton.setOnAction(event -> {
                    editingTask[0] = task;
                    taskTitleField.setText(task.getTitle());
                    startTimeBox.setValue(formatTime(task.getStartTime()));
                    endTimeBox.setValue(formatTime(task.getEndTime()));
                    taskTypeBox.setValue(task.getTaskType());
                    formTitle.setText("Edit Task");
                    saveTaskButton.setText("Save Changes");
                    formStatusLabel.setText("Editing task.");
                });

                Button deleteButton = new Button("Delete");
                deleteButton.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white;");
                deleteButton.setOnAction(event -> {
                    boolean wasEditingCurrentTask = task.equals(editingTask[0]);
                    deleteTask(task);
                    if (wasEditingCurrentTask) {
                        editingTask[0] = null;
                        taskTitleField.clear();
                        startTimeBox.setValue(null);
                        endTimeBox.setValue(null);
                        taskTypeBox.setValue(null);
                        formTitle.setText("New Task");
                        saveTaskButton.setText("Create Task");
                    }
                    formStatusLabel.setText("Task deleted.");
                });

                HBox actions = new HBox(8, editButton, deleteButton);
                actions.setAlignment(Pos.CENTER_RIGHT);

                HBox row = new HBox(12, taskInfo, spacer, actions);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(10));
                row.setStyle("-fx-background-color: #1f2937; -fx-background-radius: 8; -fx-border-color: " + accentColor + "; -fx-border-width: 0 0 0 4; -fx-border-radius: 8;");

                setText(null);
                setGraphic(row);
            }
        });

        VBox.setVgrow(taskListView, Priority.ALWAYS);

        Button logoutButton = new Button("Log Out");
        logoutButton.setOnAction(event -> showLoginScene());
        logoutButton.setStyle("-fx-background-color: #334155; -fx-text-fill: white; -fx-font-weight: bold;");

        content.getChildren().addAll(title, subtitle, sectionLabel, form, taskListView, logoutButton);
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #0f172a; -fx-background-color: transparent;");
        root.setCenter(scrollPane);
        return new Scene(root, 760, 640);
    }

    static final class FocusTask {
        private String title;
        private LocalTime startTime;
        private LocalTime endTime;
        private String taskType;

        FocusTask(String title, LocalTime startTime, LocalTime endTime, String taskType) {
            this.title = title;
            this.startTime = startTime;
            this.endTime = endTime;
            this.taskType = taskType;
        }

        String getTitle() {
            return title;
        }

        void setTitle(String title) {
            this.title = title;
        }

        LocalTime getStartTime() {
            return startTime;
        }

        void setStartTime(LocalTime startTime) {
            this.startTime = startTime;
        }

        LocalTime getEndTime() {
            return endTime;
        }

        void setEndTime(LocalTime endTime) {
            this.endTime = endTime;
        }

        String getTaskType() {
            return taskType;
        }

        void setTaskType(String taskType) {
            this.taskType = taskType;
        }
    }
}
