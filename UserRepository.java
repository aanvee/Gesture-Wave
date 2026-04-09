package auth;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles all file I/O for user data.
 *
 * File format — each user occupies 4 lines:
 *   username: <value>
 *   Password: <plain_placeholder>
 *   hashed_password: <sha256_hex>
 *   --- (separator)
 *
 * Located at: users.dat in the project root folder.
 */
public class UserRepository {

    // File located in the project folder (working directory)
    private static final String FILE_PATH = "users.dat";

    /**
     * Registers a new user.
     * Checks for duplicate usernames before writing.
     *
     * @param username      The desired username.
     * @param plainPassword The plain-text password (will be hashed before storage).
     * @return RegisterResult indicating success or the reason for failure.
     */
    public RegisterResult registerUser(String username, String plainPassword) {
        if (username == null || username.trim().isEmpty()) {
            return RegisterResult.EMPTY_USERNAME;
        }
        if (plainPassword == null || plainPassword.isEmpty()) {
            return RegisterResult.EMPTY_PASSWORD;
        }

        // Check for duplicate username
        if (usernameExists(username.trim())) {
            return RegisterResult.ALREADY_EXISTS;
        }

        // Hash the password
        String hashedPassword = PasswordHasher.hash(plainPassword);

        // Append the new user to the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            writer.write("username: " + username.trim());
            writer.newLine();
            writer.write("Password: " + plainPassword);   // plain stored for readability as per spec
            writer.newLine();
            writer.write("hashed_password: " + hashedPassword);
            writer.newLine();
            writer.write("---");
            writer.newLine();
            return RegisterResult.SUCCESS;
        } catch (IOException e) {
            e.printStackTrace();
            return RegisterResult.IO_ERROR;
        }
    }

    /**
     * Attempts to log in a user.
     * Reads the file, finds the matching username line, then
     * compares the SHA-256 hash of the supplied password with the stored hash.
     *
     * @param username      The username to look up.
     * @param plainPassword The plain-text password to verify.
     * @return LoginResult indicating success or the reason for failure.
     */
    public LoginResult loginUser(String username, String plainPassword) {
        if (username == null || username.trim().isEmpty()) {
            return LoginResult.EMPTY_USERNAME;
        }
        if (plainPassword == null || plainPassword.isEmpty()) {
            return LoginResult.EMPTY_PASSWORD;
        }

        List<String> lines = readAllLines();
        if (lines.isEmpty()) {
            return LoginResult.USER_NOT_FOUND;
        }

        String targetUsername = username.trim();

        // Walk through blocks of 4 lines (username / Password / hashed_password / ---)
        for (int i = 0; i + 2 < lines.size(); i++) {
            String userLine = lines.get(i);

            if (userLine.startsWith("username: ")) {
                String storedUsername = userLine.substring("username: ".length()).trim();

                if (storedUsername.equalsIgnoreCase(targetUsername)) {
                    // Found the user — now get the hashed_password line
                    // It is always 2 lines below the username line
                    if (i + 2 < lines.size()) {
                        String hashLine = lines.get(i + 2);
                        if (hashLine.startsWith("hashed_password: ")) {
                            String storedHash = hashLine.substring("hashed_password: ".length()).trim();
                            if (PasswordHasher.verify(plainPassword, storedHash)) {
                                return LoginResult.SUCCESS;
                            } else {
                                return LoginResult.WRONG_PASSWORD;
                            }
                        }
                    }
                }
            }
        }

        return LoginResult.USER_NOT_FOUND;
    }

    /**
     * Checks whether a username already exists in the file.
     */
    private boolean usernameExists(String username) {
        for (String line : readAllLines()) {
            if (line.startsWith("username: ")) {
                String stored = line.substring("username: ".length()).trim();
                if (stored.equalsIgnoreCase(username)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Reads all lines from the data file.
     * Returns an empty list if the file does not yet exist.
     */
    private List<String> readAllLines() {
        List<String> lines = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return lines;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    /**
     * Returns the absolute path of the data file (useful for display in the UI).
     */
    public String getFilePath() {
        return new File(FILE_PATH).getAbsolutePath();
    }

    // ---------------------------------------------------------------
    // Result enumerations
    // ---------------------------------------------------------------

    public enum RegisterResult {
        SUCCESS,
        ALREADY_EXISTS,
        EMPTY_USERNAME,
        EMPTY_PASSWORD,
        IO_ERROR
    }

    public enum LoginResult {
        SUCCESS,
        USER_NOT_FOUND,
        WRONG_PASSWORD,
        EMPTY_USERNAME,
        EMPTY_PASSWORD
    }
}