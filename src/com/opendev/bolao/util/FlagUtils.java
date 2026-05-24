package com.opendev.bolao.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utilitário para mapear países para códigos ISO-3166, bandeiras em emoji e assets PNG.
 */
public final class FlagUtils {

	private static final String FLAGS_RESOURCE = "flags.properties";
	private static final String ASSET_BASE_PATH = "/img/bandeiras/";

	private static final Map<String, String> COUNTRY_TO_ISO = loadCountryMapping();
	private static final Set<String> ISO_CODES = Collections.unmodifiableSet(
			COUNTRY_TO_ISO.values().stream()
					.map(code -> code.toLowerCase(Locale.ROOT))
					.collect(Collectors.toSet()));

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
			codigo = COUNTRY_TO_ISO.get(normalized.replace(" ", ""));
		}
		return codigo != null ? codigo : "";
	}

	public static Optional<String> assetPathForCountry(String nomePais) {
		return assetPathForCode(countryCodeFromName(nomePais));
	}

	public static Optional<String> assetPathForCode(String codigoPais) {
		if (!hasAssetForCode(codigoPais)) {
			return Optional.empty();
		}
		String sanitized = codigoPais.toLowerCase(Locale.ROOT);
		return Optional.of(ASSET_BASE_PATH + sanitized + ".png");
	}

	public static boolean hasAssetForCountry(String nomePais) {
		return hasAssetForCode(countryCodeFromName(nomePais));
	}

	public static boolean hasAssetForCode(String codigoPais) {
		if (codigoPais == null || codigoPais.isBlank()) {
			return false;
		}
		return ISO_CODES.contains(codigoPais.toLowerCase(Locale.ROOT));
	}

	public static String emojiFromCountryCode(String codigoPais) {
		if (codigoPais == null) {
			return "";
		}
		String sanitized = codigoPais.trim();
		if (sanitized.length() != 2) {
			return "";
		}
		String upper = sanitized.toUpperCase(Locale.ROOT);
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
			return sanitizeAcronym(codigoPais);
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

	private static String sanitizeAcronym(String codigoPais) {
		String sanitized = codigoPais.replace("-", " ").trim();
		if (sanitized.length() >= 2) {
			return sanitized.substring(0, 2).toUpperCase(Locale.ROOT);
		}
		return sanitized.toUpperCase(Locale.ROOT);
	}

	private static Map<String, String> loadCountryMapping() {
		try (InputStream input = FlagUtils.class.getClassLoader().getResourceAsStream(FLAGS_RESOURCE)) {
			if (input == null) {
				throw new IllegalStateException("Recurso " + FLAGS_RESOURCE + " não encontrado na classpath.");
			}
			return readMapping(input);
		}
		catch (IOException ex) {
			throw new IllegalStateException("Falha ao carregar " + FLAGS_RESOURCE, ex);
		}
	}

	private static Map<String, String> readMapping(InputStream input) throws IOException {
		Map<String, String> mapping = new HashMap<>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
			String rawLine;
			while ((rawLine = reader.readLine()) != null) {
				String line = rawLine.trim();
				if (line.isEmpty() || line.startsWith("#")) {
					continue;
				}
				int separator = line.indexOf('=');
				if (separator < 0) {
					continue;
				}
				String key = line.substring(0, separator).trim();
				String value = line.substring(separator + 1).trim();
				if (key.isEmpty() || value.isEmpty()) {
					continue;
				}
				registerMapping(mapping, key, value);
			}
		}
		return Collections.unmodifiableMap(mapping);
	}

	private static void registerMapping(Map<String, String> mapping, String key, String value) {
		mapping.put(key, value);
		String normalizedKey = normalize(key);
		if (!normalizedKey.isEmpty()) {
			mapping.put(normalizedKey, value);
			String condensed = normalizedKey.replace(" ", "");
			if (!condensed.isEmpty()) {
				mapping.put(condensed, value);
			}
		}
	}

	private static String normalize(String value) {
		String decomposed = Normalizer.normalize(value, Normalizer.Form.NFD);
		String withoutMarks = decomposed.replaceAll("\\p{M}", "");
		String collapsedSpaces = withoutMarks.replaceAll("\\s+", " ");
		return collapsedSpaces.toLowerCase(Locale.ROOT).trim();
	}
}
