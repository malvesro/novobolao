package com.opendev.bolao.infrastructure;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.jar.JarEntry;

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
		String sanitized = rawTimestamp != null ? rawTimestamp.trim() : "";
		if (sanitized.isEmpty() || sanitized.contains("${") || "n/a".equalsIgnoreCase(sanitized)) {
			return fallbackTimestamp();
		}
		try {
			OffsetDateTime parsed = OffsetDateTime.parse(sanitized);
			return OUTPUT_FORMAT.format(parsed.atZoneSameInstant(TARGET_ZONE));
		}
		catch (DateTimeParseException ignored) {
			return sanitized;
		}
	}

	public String getVersion() {
		return version;
	}

	public String getBuildDateTime() {
		return buildDateTime;
	}

	private static String fallbackTimestamp() {
		return detectBuildInstant()
				.map(instant -> OUTPUT_FORMAT.format(instant.atZone(TARGET_ZONE)))
				.orElse("N/A");
	}

	private static Optional<Instant> detectBuildInstant() {
		URL resource = BuildInfo.class.getResource("BuildInfo.class");
		if (resource == null) {
			return Optional.empty();
		}
		try {
			String protocol = resource.getProtocol();
			if ("jar".equalsIgnoreCase(protocol)) {
				JarURLConnection connection = (JarURLConnection) resource.openConnection();
				JarEntry entry = connection.getJarEntry();
				if (entry != null && entry.getTime() > 0) {
					return Optional.of(Instant.ofEpochMilli(entry.getTime()));
				}
				if (connection.getJarFile() != null && connection.getJarFile().getEntry("META-INF/MANIFEST.MF") != null) {
					long manifestTime = connection.getJarFile().getEntry("META-INF/MANIFEST.MF").getTime();
					if (manifestTime > 0) {
						return Optional.of(Instant.ofEpochMilli(manifestTime));
					}
				}
			}
			else if ("file".equalsIgnoreCase(protocol)) {
				return Optional.of(Files.getLastModifiedTime(Paths.get(resource.toURI())).toInstant());
			}
		}
		catch (IOException | URISyntaxException ignored) {
			// Fallback handled below
		}
		return Optional.of(Instant.now());
	}
}
