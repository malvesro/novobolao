package com.opendev.bolao.util;

import org.springframework.security.crypto.password.PasswordEncoder;
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
    public String encode(CharSequence rawPassword) {
        return "{" + this.idForEncode + "}" + this.encoderForEncode.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (encodedPassword == null) {
            return false;
        }
        String id = extractId(encodedPassword);
        PasswordEncoder delegate = this.idToEncoder.get(id);
        if (delegate == null) {
            // Fallback to SHA-1 for old passwords without a prefix
            // Assuming "sha1" is the key for the legacy encoder in the map
            delegate = this.idToEncoder.get("sha1");
            return delegate != null && delegate.matches(rawPassword, encodedPassword);
        }
        String rawEncodedPassword = extractEncodedPassword(encodedPassword);
        return delegate.matches(rawPassword, rawEncodedPassword);
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
}
