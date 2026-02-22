package com.opendev.bolao.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class FlagUtilsTest {

    @Test
    public void mappingShouldReturnIsoCodes() {
        assertEquals("br", FlagUtils.countryCodeFromName("Brasil"));
        assertEquals("ar", FlagUtils.countryCodeFromName("Argentina"));
        assertEquals("uy", FlagUtils.countryCodeFromName("Uruguai"));
        assertEquals("cl", FlagUtils.countryCodeFromName("Chile"));
        assertEquals("fr", FlagUtils.countryCodeFromName("França"));
        assertEquals("es", FlagUtils.countryCodeFromName("Espanha"));
        assertEquals("gb", FlagUtils.countryCodeFromName("Inglaterra"));
    }

    @Test
    public void fallbackShouldGenerateLettersWhenCodeMissing() {
        assertEquals("BR", FlagUtils.fallbackAcronym("", "Brasil"));
        assertEquals("AR", FlagUtils.fallbackAcronym(null, "Argentina"));
        assertEquals("MX", FlagUtils.fallbackAcronym(null, "México"));
    }

    @Test
    public void emojiShouldBeReturnedForIsoCode() {
        assertEquals("\uD83C\uDDE7\uD83C\uDDF7", FlagUtils.emojiFromCountryCode("br"));
        assertEquals("\uD83C\uDDE6\uD83C\uDDF7", FlagUtils.emojiFromCountryCode("ar"));
    }
}
