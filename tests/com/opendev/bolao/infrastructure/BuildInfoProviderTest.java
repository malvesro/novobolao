package com.opendev.bolao.infrastructure;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockServletContext;

class BuildInfoProviderTest {

	@Test
	void shouldExposeBuildInfoAndPublishToServletContext() {
		BuildInfoProvider provider = new BuildInfoProvider();
		BuildInfo info = provider.getBuildInfo();

		assertNotNull(info);
		assertFalse(info.getVersion().isBlank(), "Versão não deve estar vazia");
		assertFalse(info.getBuildDateTime().isBlank(), "Data/hora do build não deve estar vazia");

		MockServletContext servletContext = new MockServletContext();
		provider.setServletContext(servletContext);

		assertSame(info, servletContext.getAttribute(BuildInfoProvider.ATTRIBUTE_NAME));
	}
}
