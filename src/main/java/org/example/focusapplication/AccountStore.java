package org.example.focusapplication;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AccountStore {
    private static final Path DATA_PATH = Paths.get(System.getProperty("user.home"), ".focus-application", "accounts.txt");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^username=(.*)$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^password=(.*)$");

    private AccountStore() {
    }

    public static synchronized void createAccount(String username, String password) {
        String normalizedUsername = normalizeUsername(username);
        List<AccountRecord> accounts = readAccounts();

        if (accounts.stream().anyMatch(account -> account.username().equalsIgnoreCase(normalizedUsername))) {
            throw new IllegalArgumentException("An account with that username already exists.");
        }

        accounts.add(new AccountRecord(normalizedUsername, PasswordHasher.hash(password.toCharArray())));
        writeAccounts(accounts);
    }

    public static synchronized boolean validateLogin(String username, String password) {
        String normalizedUsername = normalizeUsername(username);
        return readAccounts().stream()
                .filter(account -> account.username().equalsIgnoreCase(normalizedUsername))
                .findFirst()
                .map(account -> PasswordHasher.verify(password.toCharArray(), account.passwordHash()))
                .orElse(false);
    }

    private static List<AccountRecord> readAccounts() {
        try {
            if (Files.notExists(DATA_PATH)) {
                createDataDirectory();
                return new ArrayList<>();
            }

            List<String> lines = Files.readAllLines(DATA_PATH, StandardCharsets.UTF_8);
            List<AccountRecord> accounts = new ArrayList<>();
            String lastUsername = null;
            String lastPasswordHash = null;

            for (String line : lines) {
                if (line == null || line.isBlank()) {
                    continue;
                }

                Matcher usernameMatcher = USERNAME_PATTERN.matcher(line);
                if (usernameMatcher.matches()) {
                    lastUsername = usernameMatcher.group(1);
                    continue;
                }

                Matcher passwordMatcher = PASSWORD_PATTERN.matcher(line);
                if (passwordMatcher.matches()) {
                    lastPasswordHash = passwordMatcher.group(1);
                    continue;
                }

                if (lastUsername != null && lastPasswordHash != null) {
                    accounts.add(new AccountRecord(lastUsername, lastPasswordHash));
                    lastUsername = null;
                    lastPasswordHash = null;
                }
            }

            if (lastUsername != null && lastPasswordHash != null) {
                accounts.add(new AccountRecord(lastUsername, lastPasswordHash));
            }

            return accounts;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read account data", e);
        }
    }

    private static void writeAccounts(List<AccountRecord> accounts) {
        try {
            createDataDirectory();
            List<String> lines = new ArrayList<>();
            for (AccountRecord account : accounts) {
                lines.add("username=" + account.username());
                lines.add("password=" + account.passwordHash());
            }
            Files.write(DATA_PATH, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write account data", e);
        }
    }

    private static void createDataDirectory() throws IOException {
        Path directory = DATA_PATH.getParent();
        if (directory != null && Files.notExists(directory)) {
            Files.createDirectories(directory);
        }
    }

    private static String normalizeUsername(String username) {
        if (username == null) {
            return "";
        }
        return username.trim();
    }

    public record AccountRecord(String username, String passwordHash) {
    }
}
