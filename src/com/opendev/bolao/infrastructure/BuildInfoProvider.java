package com.opendev.bolao.infrastructure;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ServletContextAware;

import jakarta.servlet.ServletContext;

/**
 * Loads build metadata from {@code version.properties} and exposes it to the UI.
 */
public class BuildInfoProvider implements ServletContextAware {

	public static final String ATTRIBUTE_NAME = "buildInfo";

	private static final Logger LOGGER = LoggerFactory.getLogger(BuildInfoProvider.class);

	private final BuildInfo buildInfo;
	private ServletContext servletContext;

	public BuildInfoProvider() {
		this.buildInfo = loadBuildInfo();
	}

	@Override
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
		publish();
	}

	public BuildInfo getBuildInfo() {
		return buildInfo;
	}

	private BuildInfo loadBuildInfo() {
		Properties properties = new Properties();
		try (InputStream input = getClass().getClassLoader().getResourceAsStream("version.properties")) {
			if (input != null) {
				properties.load(input);
			}
			else {
				LOGGER.warn("Recurso version.properties não encontrado na classpath. Usando valores padrão.");
			}
		}
		catch (IOException ex) {
			LOGGER.warn("Falha ao carregar version.properties: {}", ex.getMessage());
		}
		return BuildInfo.from(properties);
	}

	private void publish() {
		if (servletContext != null) {
			servletContext.setAttribute(ATTRIBUTE_NAME, buildInfo);
		}
	}
}
