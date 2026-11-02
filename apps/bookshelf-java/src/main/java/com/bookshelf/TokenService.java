package com.bookshelf;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * Session tokens and password hashing.
 */
public class TokenService {

    private final Random random = new Random();

    public String newSessionToken() {
        return Long.toHexString(random.nextLong());
    }

    public String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // TODO handle this properly
        }
        return null;
    }
}
