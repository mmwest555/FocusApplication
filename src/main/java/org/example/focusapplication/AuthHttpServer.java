package org.example.focusapplication;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AuthHttpServer {
    private static final int PORT = 8081;
    private static HttpServer server;

    private static final Pattern USERNAME_PATTERN = Pattern.compile("\"username\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("\"password\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");

    private AuthHttpServer() {
    }

    public static synchronized void startServer() {
        if (server != null) {
            return;
        }

        try {
            server = HttpServer.create(new InetSocketAddress(PORT), 0);
            server.createContext("/api/accounts", AuthHttpServer::handleCreateAccount);
            server.createContext("/api/auth/login", AuthHttpServer::handleLogin);
            server.createContext("/api/health", exchange -> sendJson(exchange, 200, "{\"status\":\"ok\"}"));
            server.setExecutor(Executors.newFixedThreadPool(4));
            server.start();
        } catch (BindException e) {
            System.out.println("Authentication server already running on port " + PORT + "; continuing without restarting.");
        } catch (IOException e) {
            throw new IllegalStateException("Unable to start authentication server", e);
        }
    }

    private static void handleCreateAccount(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendJson(exchange, 405, "{\"message\":\"Method not allowed\"}");
            return;
        }

        String payload = readBody(exchange);
        String username = extractField(payload, USERNAME_PATTERN);
        String password = extractField(payload, PASSWORD_PATTERN);

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            sendJson(exchange, 400, "{\"message\":\"Username and password are required\"}");
            return;
        }

        try {
            AccountStore.createAccount(username, password);
            sendJson(exchange, 201, "{\"message\":\"Account created successfully\"}");
        } catch (IllegalArgumentException e) {
            sendJson(exchange, 409, "{\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
        } catch (Exception e) {
            sendJson(exchange, 500, "{\"message\":\"Unable to create account\"}");
        }
    }

    private static void handleLogin(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendJson(exchange, 405, "{\"message\":\"Method not allowed\"}");
            return;
        }

        String payload = readBody(exchange);
        String username = extractField(payload, USERNAME_PATTERN);
        String password = extractField(payload, PASSWORD_PATTERN);

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            sendJson(exchange, 400, "{\"message\":\"Username and password are required\"}");
            return;
        }

        boolean valid = AccountStore.validateLogin(username, password);
        if (valid) {
            sendJson(exchange, 200, "{\"message\":\"Login successful\"}");
        } else {
            sendJson(exchange, 401, "{\"message\":\"Invalid username or password\"}");
        }
    }

    private static String readBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private static String extractField(String payload, Pattern pattern) {
        Matcher matcher = pattern.matcher(payload);
        if (matcher.find()) {
            return matcher.group(1).replace("\\\"", "\"").replace("\\\\", "\\");
        }
        return "";
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static void sendJson(HttpExchange exchange, int statusCode, String jsonPayload) throws IOException {
        byte[] response = jsonPayload.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(response);
        }
    }
}
