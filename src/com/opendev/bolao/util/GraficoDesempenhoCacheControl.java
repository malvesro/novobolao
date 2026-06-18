package com.opendev.bolao.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Controle global de versão de cache para dados do gráfico de desempenho.
 *
 * A cada atualização administrativa de resultado, a versão é incrementada para
 * invalidar caches em memória que dependem do histórico de pontuação.
 */
public final class GraficoDesempenhoCacheControl {

    private static final AtomicLong CACHE_VERSION = new AtomicLong(1L);

    private GraficoDesempenhoCacheControl() {
        // utilitário estático
    }

    public static long obterVersaoAtual() {
        return CACHE_VERSION.get();
    }

    public static long invalidarCacheGlobal() {
        return CACHE_VERSION.incrementAndGet();
    }
}
