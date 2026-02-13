package com.opendev.bolao.util;


public class StringUtils {
    
    public static final String formatarNomeCompleto(String nome) {
        StringBuffer formatado = new StringBuffer();
        nome = nome.trim();
        String[] nomes = nome.split(" ");
        for (int i = 0; i < nomes.length; i++) {
            if (i == 0 || i == (nomes.length - 1)) {
                formatado.append(capitalizar(nomes[i]));
            } else if (nomes[i].equalsIgnoreCase("de") || nomes[i].equalsIgnoreCase("do")
                    || nomes[i].equalsIgnoreCase("da") || nomes[i].equalsIgnoreCase("dos")
                    || nomes[i].equalsIgnoreCase("das")) {
                continue;
            } else {
                formatado.append(Character.toUpperCase(nomes[i].charAt(0)) + ".");
            }
            formatado.append(" ");
        }
        return formatado.toString().trim();
    }
    
    private static final String capitalizar(String s) {
        String capitalizada = s.toLowerCase();
        capitalizada = Character.toUpperCase(capitalizada.charAt(0)) + capitalizada.substring(1);
        return capitalizada;
    }

}
