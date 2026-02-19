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
        byte[] raw = sha1(rawPassword);
        return Base64.getEncoder().encodeToString(raw);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        byte[] raw = sha1(rawPassword);
        String encodedRaw = Base64.getEncoder().encodeToString(raw);
        if (encodedRaw.equals(encodedPassword)) {
            return true;
        }
        // Compatibilidade com legado que persiste SHA-1 em hex
        String hexRaw = toHex(raw);
        return hexRaw.equalsIgnoreCase(encodedPassword);
    }

    private byte[] sha1(CharSequence rawPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            digest.update(rawPassword.toString().getBytes(ENCODING));
            return digest.digest();
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
