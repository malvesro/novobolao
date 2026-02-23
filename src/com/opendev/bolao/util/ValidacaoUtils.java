package com.opendev.bolao.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class ValidacaoUtils {

    private static boolean evalRegExp(String regExp, String value) {
        Pattern pattern = Pattern.compile(regExp);
        Matcher matcher = pattern.matcher(value);
        return matcher.matches();
    }
    
    public static boolean isEmailValido(String email) {
        String regExp = "^(\\w|[-])+(\\.(\\w|[-])+)*@((\\[([0-1]?\\d?\\d|2" +
                "[0-4]\\d|25[0-5])\\.([0-1]?\\d?\\d|2[0-4]\\d|25[0-5])\\.(" +
                "[0-1]?\\d?\\d|2[0-4]\\d|25[0-5])\\.([0-1]?\\d?\\d|2[0-4]\\" +
                "d|25[0-5])\\])|((([a-zA-Z0-9])+(([-])+([a-zA-Z0-9])+)*\\" +
                ".)+([a-zA-Z])+(([-])+([a-zA-Z0-9])+)*))$";
        return evalRegExp(regExp, email);
    }
    
    public static boolean isSenhaValida(String senha) {
        if (isVazia(senha)) {
            return false;
        }

        String trimmed = senha.trim();
        if (trimmed.length() < 8 || trimmed.length() > 64) {
            return false;
        }

        if (senha.codePoints().anyMatch(Character::isISOControl)) {
            return false;
        }

        for (int index = 0; index < trimmed.length(); index++) {
            char caractere = trimmed.charAt(index);
            if (Character.isISOControl(caractere)) {
                return false;
            }
        }

        return true;
    }
    
    public static boolean isVazia(String s) {
        return s == null || s.trim().length() == 0;
    }
}
