package com.opendev.bolao.util;

import org.springframework.security.crypto.password.PasswordEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * PasswordEncoder implementation that matches the legacy Acegi SHA-1 Base64 encoding.
 * Used during migration to Spring Security 6.
 */
public class LegacySha1PasswordEncoder implements PasswordEncoder {

    private static final String ENCODING = "UTF-8";
    private static final String ALGORITHM = "SHA-1";

    @Override
    public String encode(CharSequence rawPassword) {
        if (rawPassword == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            digest.update(rawPassword.toString().getBytes(ENCODING));
            byte[] raw = digest.digest();
            return Base64.getEncoder().encodeToString(raw);
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        String encodedRaw = encode(rawPassword);
        return encodedRaw.equals(encodedPassword);
    }
}
