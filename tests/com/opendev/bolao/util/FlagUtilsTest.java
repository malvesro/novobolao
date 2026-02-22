package com.opendev.bolao.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

public class FlagUtilsTest {

	@Test
	public void mappingShouldReturnIsoCodes() {
		assertEquals("br", FlagUtils.countryCodeFromName("Brasil"));
		assertEquals("ar", FlagUtils.countryCodeFromName("Argentina"));
		assertEquals("uy", FlagUtils.countryCodeFromName("Uruguai"));
		assertEquals("fr", FlagUtils.countryCodeFromName("França"));
		assertEquals("es", FlagUtils.countryCodeFromName("Espanha"));
		assertEquals("gb-eng", FlagUtils.countryCodeFromName("Inglaterra"));
		assertEquals("gb-sct", FlagUtils.countryCodeFromName("Escócia"));
		assertEquals("gb-wls", FlagUtils.countryCodeFromName("País de Gales"));
		assertEquals("", FlagUtils.countryCodeFromName("Seleção Inexistente"));
	}

	@Test
	public void fallbackShouldGenerateLettersWhenCodeMissing() {
		assertEquals("BR", FlagUtils.fallbackAcronym("", "Brasil"));
		assertEquals("AR", FlagUtils.fallbackAcronym(null, "Argentina"));
		assertEquals("ME", FlagUtils.fallbackAcronym(null, "México"));
		assertEquals("GB", FlagUtils.fallbackAcronym("gb-eng", "Inglaterra"));
	}

	@Test
	public void emojiShouldBeReturnedForIsoCode() {
		assertEquals("\uD83C\uDDE7\uD83C\uDDF7", FlagUtils.emojiFromCountryCode("br"));
		assertEquals("\uD83C\uDDE6\uD83C\uDDF7", FlagUtils.emojiFromCountryCode("ar"));
		assertEquals("", FlagUtils.emojiFromCountryCode("gb-eng"));
	}

	@Test
	public void shouldExposeAssetPathWhenAvailable() {
		assertTrue(FlagUtils.hasAssetForCountry("Inglaterra"));
		Optional<String> assetPath = FlagUtils.assetPathForCountry("Inglaterra");
		assertTrue(assetPath.isPresent());
		assertEquals("/img/bandeiras/gb-eng.png", assetPath.orElseThrow());
	}

	@Test
	public void shouldReturnEmptyWhenAssetMissing() {
		assertFalse(FlagUtils.hasAssetForCountry("Pais Inexistente"));
		assertTrue(FlagUtils.assetPathForCountry("Pais Inexistente").isEmpty());
	}

	@Test
	public void mappingShouldContainPaisDeGales() throws Exception {
		Field field = FlagUtils.class.getDeclaredField("COUNTRY_TO_ISO");
		field.setAccessible(true);
		@SuppressWarnings("unchecked")
		Map<String, String> mapping = (Map<String, String>) field.get(null);
		assertTrue(mapping.containsKey("pais de gales"));
		assertEquals("gb-wls", mapping.get("pais de gales"));
	}
}
