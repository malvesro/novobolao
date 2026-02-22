package com.opendev.bolao.util;

import java.text.Normalizer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Utilitário para mapear países para códigos ISO-3166 e bandeiras em emoji.
 * Mantém o mapeamento em memória para evitar dependência de imagens estáticas.
 */
public final class FlagUtils {

	private static final Map<String, String> COUNTRY_TO_ISO;

	static {
		Map<String, String> mapping = new HashMap<>();
		mapping.put("brasil", "br");
		mapping.put("argentina", "ar");
		mapping.put("uruguai", "uy");
		mapping.put("chile", "cl");
		mapping.put("alemanha", "de");
		mapping.put("franca", "fr");
		mapping.put("inglaterra", "gb");
		mapping.put("espanha", "es");
		mapping.put("portugal", "pt");
		mapping.put("italia", "it");
		mapping.put("holanda", "nl");
		mapping.put("belgica", "be");
		mapping.put("mexico", "mx");
		mapping.put("estados unidos", "us");
		mapping.put("canada", "ca");
		mapping.put("costa rica", "cr");
		COUNTRY_TO_ISO = Collections.unmodifiableMap(mapping);
	}

	private FlagUtils() {
		// utility class
	}

	public static String countryCodeFromName(String nomePais) {
		if (nomePais == null || nomePais.isEmpty()) {
			return "";
		}
		String normalized = normalize(nomePais);
		String codigo = COUNTRY_TO_ISO.get(normalized);
		if (codigo == null) {
			// tentar novamente ignorando espaços redundantes
			codigo = COUNTRY_TO_ISO.get(normalized.replace(" ", ""));
		}
		return codigo != null ? codigo : "";
	}

	public static String emojiFromCountryCode(String codigoPais) {
		if (codigoPais == null || codigoPais.length() != 2) {
			return "";
		}
		String upper = codigoPais.toUpperCase(Locale.ROOT);
		int firstCodePoint = Character.codePointAt(upper, 0);
		int secondCodePoint = Character.codePointAt(upper, 1);
		if (!Character.isLetter(firstCodePoint) || !Character.isLetter(secondCodePoint)) {
			return "";
		}
		int base = 0x1F1E6;
		int emojiFirst = base + (firstCodePoint - 'A');
		int emojiSecond = base + (secondCodePoint - 'A');
		return new String(Character.toChars(emojiFirst)) + new String(Character.toChars(emojiSecond));
	}

	public static String fallbackAcronym(String codigoPais, String nomePais) {
		if (codigoPais != null && !codigoPais.isEmpty()) {
			return codigoPais.toUpperCase(Locale.ROOT);
		}
		if (nomePais == null || nomePais.isEmpty()) {
			return "--";
		}
		String normalized = normalize(nomePais).replace(" ", "");
		if (normalized.length() >= 2) {
			return normalized.substring(0, 2).toUpperCase(Locale.ROOT);
		}
		return nomePais.substring(0, Math.min(nomePais.length(), 2)).toUpperCase(Locale.ROOT);
	}

	private static String normalize(String value) {
		String decomposed = Normalizer.normalize(value, Normalizer.Form.NFD);
		return decomposed.replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT).trim();
	}
}
