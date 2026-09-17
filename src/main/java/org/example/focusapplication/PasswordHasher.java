package org.example.focusapplication;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public final class PasswordHasher {
    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH = 256;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

    private PasswordHasher() {
    }

    public static String hash(char[] password) {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);

        byte[] hash = hash(password, salt);
        return "PBKDF2$" + ITERATIONS + "$" + Base64.getEncoder().encodeToString(salt) + "$" + Base64.getEncoder().encodeToString(hash);
    }

    public static boolean verify(char[] password, String storedHash) {
        if (storedHash == null || storedHash.isBlank()) {
            return false;
        }

        String[] parts = storedHash.split("\\$");
        if (parts.length != 4 || !"PBKDF2".equals(parts[0])) {
            return false;
        }

        int iterations;
        try {
            iterations = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return false;
        }

        byte[] salt = Base64.getDecoder().decode(parts[2]);
        byte[] expectedHash = Base64.getDecoder().decode(parts[3]);
        byte[] actualHash = hash(password, salt, iterations);

        return constantTimeEquals(actualHash, expectedHash);
    }

    private static byte[] hash(char[] password, byte[] salt) {
        return hash(password, salt, ITERATIONS);
    }

    private static byte[] hash(char[] password, byte[] salt, int iterations) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            return factory.generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to hash password", e);
        }
    }

    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a == null || b == null || a.length != b.length) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
    }
}
