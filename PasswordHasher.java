package com.example.todologin;

import android.util.Base64;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Password storage helper using PBKDF2-HMAC-SHA256 with a random per-user salt.
 * This is intentionally stronger than MD5/SHA-1/SHA-256(password) alone.
 */
public final class PasswordHasher {
    private static final int SALT_BYTES = 16;
    private static final int HASH_BITS = 256;
    private static final int ITERATIONS = 210_000;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordHasher() { }

    public static String newSalt() {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        return Base64.encodeToString(salt, Base64.NO_WRAP);
    }

    public static String hash(String password, String saltBase64)
            throws GeneralSecurityException {
        byte[] salt = Base64.decode(saltBase64, Base64.NO_WRAP);
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, HASH_BITS);
        try {
            byte[] derived = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec).getEncoded();
            return Base64.encodeToString(derived, Base64.NO_WRAP);
        } finally {
            spec.clearPassword();
        }
    }

    public static boolean verify(String password, String salt, String expectedHash)
            throws GeneralSecurityException {
        byte[] actual = Base64.decode(hash(password, salt), Base64.NO_WRAP);
        byte[] expected = Base64.decode(expectedHash, Base64.NO_WRAP);
        if (actual.length != expected.length) return false;
        int difference = 0;
        for (int i = 0; i < actual.length; i++) {
            difference |= actual[i] ^ expected[i];
        }
        return difference == 0;
    }
}
