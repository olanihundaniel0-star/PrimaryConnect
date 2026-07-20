package com.primaryconnect.service;

import com.primaryconnect.data.UserDAO;
import com.primaryconnect.model.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Handles login authentication for application users.
 */
public class AuthManager {
    private static final String HASH_ALGORITHM = "SHA-256";

    private final UserDAO userDAO;

    public AuthManager() {
        this(new UserDAO());
    }

    public AuthManager(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public User login(String username, String plainPassword) {
        User user = userDAO.findByUsername(username);
        if (user == null) {
            return null;
        }

        String passwordHash = hashPassword(plainPassword);
        if (passwordHash.equals(user.getPasswordHash())) {
            return user;
        }

        return null;
    }

    public String hashPassword(String plainPassword) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hashBytes = messageDigest.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            return toHex(hashBytes);
        } catch (NoSuchAlgorithmException exception) {
            throw new RuntimeException("Failed to hash password with " + HASH_ALGORITHM + ".", exception);
        }
    }

    private String toHex(byte[] bytes) {
        StringBuilder hexBuilder = new StringBuilder(bytes.length * 2);
        for (byte currentByte : bytes) {
            String hex = Integer.toHexString(currentByte & 0xff);
            if (hex.length() == 1) {
                hexBuilder.append('0');
            }
            hexBuilder.append(hex);
        }
        return hexBuilder.toString();
    }
}
