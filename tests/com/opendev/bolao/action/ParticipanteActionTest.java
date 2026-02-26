package com.opendev.bolao.action;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.List;

import org.jfree.data.category.DefaultCategoryDataset;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.opendev.bolao.grafico.GraficoBarraLideres;
import com.opendev.bolao.model.Participante;
import com.opendev.bolao.service.ParticipanteService;
import com.opendev.bolao.util.MensagemErro;

class ParticipanteActionTest {

    private MockHttpServletRequest request;

    @BeforeEach
    void configurarRequest() {
        request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void limparRequest() {
        RequestContextHolder.resetRequestAttributes();
    }

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

    @Test
    @DisplayName("cadastrar deve retornar input quando login ja existir")
    void deveBloquearCadastroComLoginDuplicado() throws Exception {
        ParticipanteService participanteService = Mockito.mock(ParticipanteService.class);
        when(participanteService.buscarPorLogin("usuario"))
                .thenReturn(new Participante());

        ParticipanteAction action = new ParticipanteAction();
        action.setParticipanteService(participanteService);
        action.setLogin("usuario");
        action.setNome("Usuario Teste");
        action.setEmail("teste@example.com");
        action.setSenha("Password@123");

        String resultado = action.cadastrar();

        assertThat(resultado).isEqualTo("input");
        List erros = action.getErrosInclusao();
        assertThat(erros).isNotNull();
        assertThat(erros).filteredOn(MensagemErro.class::isInstance)
                .extracting(obj -> ((MensagemErro) obj).getMensagem())
                .contains("Ja existe um cadastro ativo com este login.");
    }

    @Test
    @DisplayName("cadastrar deve retornar input quando e-mail ja existir")
    void deveBloquearCadastroComEmailDuplicado() throws Exception {
        ParticipanteService participanteService = Mockito.mock(ParticipanteService.class);
        when(participanteService.buscarPorLogin("usuario"))
                .thenReturn(null);
        when(participanteService.buscarPorEmail("mail@example.com"))
                .thenReturn(new Participante());

        ParticipanteAction action = new ParticipanteAction();
        action.setParticipanteService(participanteService);
        action.setLogin("usuario");
        action.setNome("Usuario Teste");
        action.setEmail("mail@example.com");
        action.setSenha("Password@123");

        String resultado = action.cadastrar();

        assertThat(resultado).isEqualTo("input");
        List erros = action.getErrosInclusao();
        assertThat(erros).isNotNull();
        assertThat(erros).filteredOn(MensagemErro.class::isInstance)
                .extracting(obj -> ((MensagemErro) obj).getMensagem())
                .contains("Este e-mail ja esta associado a outro cadastro.");
    }
}
