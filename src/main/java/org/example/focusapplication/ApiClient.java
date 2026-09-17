package org.example.focusapplication;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ApiClient {
    private static final String BASE_URL = "http://localhost:8081";
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final Pattern MESSAGE_PATTERN = Pattern.compile("\"message\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");

    private ApiClient() {
    }

    public static ApiResult createAccount(String username, String password) throws IOException, InterruptedException {
        String payload = String.format("{\"username\":\"%s\",\"password\":\"%s\"}",
                escapeJson(username), escapeJson(password));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/accounts"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        return fromResponse(response);
    }

    public static ApiResult login(String username, String password) throws IOException, InterruptedException {
        String payload = String.format("{\"username\":\"%s\",\"password\":\"%s\"}",
                escapeJson(username), escapeJson(password));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        return fromResponse(response);
    }

    private static ApiResult fromResponse(HttpResponse<String> response) {
        String body = response.body() == null ? "" : response.body();
        String message = extractMessage(body);

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return new ApiResult(true, message.isEmpty() ? "Success" : message);
        }

        return new ApiResult(false, message.isEmpty() ? "Request failed." : message);
    }

    private static String extractMessage(String json) {
        Matcher matcher = MESSAGE_PATTERN.matcher(json);
        if (matcher.find()) {
            return matcher.group(1).replace("\\\"", "\"").replace("\\\\", "\\");
        }
        return "";
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    public record ApiResult(boolean success, String message) {
    }
}
