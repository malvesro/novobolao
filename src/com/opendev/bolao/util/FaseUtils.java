package com.opendev.bolao.util;

import com.opendev.bolao.model.Jogo;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Utilitário para mapear códigos de fase do Jogo para descrições localizadas.
 */
public final class FaseUtils {

    private static final Map<Integer, String> MESSAGE_KEYS = Map.of(
        Jogo.FASE_GRUPO_RODADA_1, "filter.fase.11",
        Jogo.FASE_GRUPO_RODADA_2, "filter.fase.12",
        Jogo.FASE_GRUPO_RODADA_3, "filter.fase.13",
        Jogo.FASE_TRINTA_DOIS_AVOS, "filter.fase.16",
        Jogo.FASE_OITAVAS, "filter.fase.8",
        Jogo.FASE_QUARTAS, "filter.fase.4",
        Jogo.FASE_SEMIFINAL, "filter.fase.2",
        Jogo.FASE_TERCEIRO_LUGAR, "filter.fase.3",
        Jogo.FASE_FINAL, "filter.fase.1"
    );

    private FaseUtils() {
    }

    public static String getMessageKey(int fase) {
        return MESSAGE_KEYS.get(fase);
    }

    public static String getDescricaoFase(int fase, Locale locale) {
        Locale effectiveLocale = locale != null ? locale : Locale.getDefault();
        String key = MESSAGE_KEYS.get(fase);
        if (key == null) {
            return "Fase " + fase;
        }
        ResourceBundle bundle = ResourceBundle.getBundle("messages", effectiveLocale);
        return bundle.containsKey(key) ? bundle.getString(key) : "Fase " + fase;
    }

    public static boolean isFaseDeGrupos(int fase) {
        return fase == Jogo.FASE_GRUPO_RODADA_1
            || fase == Jogo.FASE_GRUPO_RODADA_2
            || fase == Jogo.FASE_GRUPO_RODADA_3;
    }
}
