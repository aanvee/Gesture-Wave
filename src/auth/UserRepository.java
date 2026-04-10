package auth;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {
    private static final String FILE_PATH = "users.dat";

    public RegisterResult registerUser(String username, String plainPassword) {
        if (username == null || username.trim().isEmpty()) {
            return RegisterResult.EMPTY_USERNAME;
        }
        if (plainPassword == null || plainPassword.isEmpty()) {
            return RegisterResult.EMPTY_PASSWORD;
        }

        if (usernameExists(username.trim())) {
            return RegisterResult.ALREADY_EXISTS;
        }
        String hashedPassword = PasswordHasher.hash(plainPassword);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            writer.write("username: " + username.trim());
            writer.newLine();
            writer.write("Password: " + plainPassword); // plain stored for readability as per spec
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

        for (int i = 0; i + 2 < lines.size(); i++) {
            String userLine = lines.get(i);

            if (userLine.startsWith("username: ")) {
                String storedUsername = userLine.substring("username: ".length()).trim();

                if (storedUsername.equalsIgnoreCase(targetUsername)) {

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

    private List<String> readAllLines() {
        List<String> lines = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists())
            return lines;

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

    public String getFilePath() {
        return new File(FILE_PATH).getAbsolutePath();
    }

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
