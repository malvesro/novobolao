package com.opendev.bolao.action;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.InputStream;

import org.jfree.data.category.DefaultCategoryDataset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.opendev.bolao.grafico.GraficoBarraLideres;
import com.opendev.bolao.service.ParticipanteService;

class ParticipanteActionTest {

    @Test
    @DisplayName("gerarGraficoLiderancaImagem deve produzir stream PNG")
    void deveGerarGraficoLiderancaImagem() throws Exception {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(15, "Participante A", "Participante A");

        ParticipanteService participanteService = Mockito.mock(ParticipanteService.class);
        when(participanteService.construirGraficoDeBarrasDosLideres())
                .thenReturn(new GraficoBarraLideres(dataset));

        ParticipanteAction action = new ParticipanteAction();
        action.setParticipanteService(participanteService);

        String resultado = action.gerarGraficoLiderancaImagem();
        assertThat(resultado).isEqualTo("success");

        InputStream stream = action.getGraficoStream();
        assertThat(stream).isNotNull();
        assertThat(stream.readAllBytes()).isNotEmpty();
    }
}
