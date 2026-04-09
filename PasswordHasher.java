package auth;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for SHA-256 password hashing.
 * Uses Java's built-in MessageDigest for cryptographic hashing.
 */
public class PasswordHasher {

    /**
     * Hashes a plain-text password using SHA-256 algorithm.
     *
     * @param plainPassword The raw password entered by the user.
     * @return A 64-character hexadecimal SHA-256 hash string.
     */
    public static String hash(String plainPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(plainPassword.getBytes());

            // Convert byte array to hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available on this JVM.", e);
        }
    }

    /**
     * Verifies a plain-text password against a stored SHA-256 hash.
     *
     * @param plainPassword  The raw password to check.
     * @param storedHash     The previously stored SHA-256 hash.
     * @return true if the hashes match, false otherwise.
     */
    public static boolean verify(String plainPassword, String storedHash) {
        return hash(plainPassword).equals(storedHash);
    }
}