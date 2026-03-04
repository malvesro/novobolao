package com.opendev.bolao.util;

import java.time.ZoneId;
import java.util.TimeZone;

/**
 * Centraliza a zona horária oficial do sistema.
 */
public final class BolaoTime {

    private static final ZoneId ZONE_ID = ZoneId.of("America/Sao_Paulo");
    private static final TimeZone TIME_ZONE = TimeZone.getTimeZone(ZONE_ID);

    private BolaoTime() {
    }

    public static ZoneId getZoneId() {
        return ZONE_ID;
    }

    public static TimeZone getTimeZone() {
        return TIME_ZONE;
    }
}
