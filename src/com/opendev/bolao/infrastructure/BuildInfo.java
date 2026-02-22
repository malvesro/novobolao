package com.opendev.bolao.infrastructure;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;

/**
 * Immutable metadata about the application build.
 */
public final class BuildInfo {

	private static final ZoneId TARGET_ZONE = ZoneId.of("America/Sao_Paulo");
	private static final DateTimeFormatter OUTPUT_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
			.withLocale(new Locale("pt", "BR"));

	private final String version;
	private final String buildDateTime;

	private BuildInfo(String version, String buildDateTime) {
		this.version = version;
		this.buildDateTime = buildDateTime;
	}

	public static BuildInfo from(Properties properties) {
		Objects.requireNonNull(properties, "properties");
		String version = properties.getProperty("app.version", "desconhecida");
		String rawTimestamp = properties.getProperty("build.timestamp", "");
		String formattedTimestamp = formatTimestamp(rawTimestamp);
		return new BuildInfo(version, formattedTimestamp);
	}

	private static String formatTimestamp(String rawTimestamp) {
		if (rawTimestamp == null || rawTimestamp.isBlank()) {
			return "N/A";
		}
		try {
			OffsetDateTime parsed = OffsetDateTime.parse(rawTimestamp.trim());
			return OUTPUT_FORMAT.format(parsed.atZoneSameInstant(TARGET_ZONE));
		}
		catch (DateTimeParseException ignored) {
			return rawTimestamp.trim();
		}
	}

	public String getVersion() {
		return version;
	}

	public String getBuildDateTime() {
		return buildDateTime;
	}
}
