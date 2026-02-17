package com.opendev.bolao.util;

import org.acegisecurity.providers.encoding.PasswordEncoder;
import org.acegisecurity.providers.encoding.ShaPasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.HashMap;
import java.util.Map;

public class DelegatingPasswordEncoder implements PasswordEncoder {

    private final String idForEncode;
    private final PasswordEncoder encoderForEncode;
    private final Map<String, PasswordEncoder> idToEncoder;

    public DelegatingPasswordEncoder(String idForEncode, Map<String, PasswordEncoder> idToEncoder) {
        if (idForEncode == null) {
            throw new IllegalArgumentException("idForEncode cannot be null");
        }
        if (!idToEncoder.containsKey(idForEncode)) {
            throw new IllegalArgumentException("idForEncode " + idForEncode + " is not mapped to a PasswordEncoder");
        }
        this.idForEncode = idForEncode;
        this.encoderForEncode = idToEncoder.get(idForEncode);
        this.idToEncoder = new HashMap<>(idToEncoder);
    }

    @Override
    public String encodePassword(String rawPass, Object salt) {
        return "{" + this.idForEncode + "}" + this.encoderForEncode.encodePassword(rawPass, salt);
    }

    @Override
    public boolean isPasswordValid(String encPass, String rawPass, Object salt) {
        if (encPass == null) {
            return false;
        }
        String id = extractId(encPass);
        PasswordEncoder delegate = this.idToEncoder.get(id);
        if (delegate == null) {
            // Fallback to SHA-1 for old passwords without a prefix
            delegate = this.idToEncoder.get("sha1");
            return delegate.isPasswordValid(encPass, rawPass, salt);
        }
        String rawEncodedPassword = extractEncodedPassword(encPass);
        return delegate.isPasswordValid(rawEncodedPassword, rawPass, salt);
    }

    private String extractId(String prefixEncodedPassword) {
        if (prefixEncodedPassword == null || !prefixEncodedPassword.contains("{") || !prefixEncodedPassword.contains("}")) {
            return null; // No prefix
        }
        int start = prefixEncodedPassword.indexOf('{');
        int end = prefixEncodedPassword.indexOf('}');
        if (start != 0 || end <= start + 1) {
            return null;
        }
        return prefixEncodedPassword.substring(start + 1, end);
    }

    private String extractEncodedPassword(String prefixEncodedPassword) {
        if (prefixEncodedPassword == null || !prefixEncodedPassword.contains("}")) {
            return prefixEncodedPassword;
        }
        int end = prefixEncodedPassword.indexOf('}');
        return prefixEncodedPassword.substring(end + 1);
    }

    // A Spring Security 5 PasswordEncoder adapted to the old Acegi PasswordEncoder interface
    public static class BCryptAcegiPasswordEncoder implements PasswordEncoder {
        private final BCryptPasswordEncoder springEncoder = new BCryptPasswordEncoder();

        @Override
        public String encodePassword(String rawPass, Object salt) {
            return springEncoder.encode(rawPass);
        }

        @Override
        public boolean isPasswordValid(String encPass, String rawPass, Object salt) {
            return springEncoder.matches(rawPass, encPass);
        }
    }
}
