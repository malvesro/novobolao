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
        if (senha.length() < 5 || senha.length() > 20) {
            return false;
        }
        if (senha.indexOf("-") != -1 || senha.indexOf("_") != -1
                || senha.indexOf("$") != -1 || senha.indexOf("%") != -1
                || senha.indexOf("-") != -1 || senha.indexOf("&") != -1
                || senha.indexOf("!") != -1 || senha.indexOf("*") != -1
                || senha.indexOf(" ") != -1 || senha.indexOf("@") != -1) {
            return false;
        }
        return true;
    }
    
    public static boolean isVazia(String s) {
        return s == null || s.trim().length() == 0;
    }
}
