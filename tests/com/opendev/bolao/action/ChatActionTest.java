package com.opendev.bolao.action;

import com.opendev.bolao.exception.BusinessException;
import com.opendev.bolao.service.ChatService;
import com.opendev.bolao.service.dto.ChatMensagemView;
import com.opendev.bolao.service.dto.MentionNotification;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChatActionTest {

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private ChatService chatService;
    private ChatAction action;
    private Level previousChatActionLogLevel;

    @BeforeEach
    void setup() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, response));

        chatService = Mockito.mock(ChatService.class);
        action = new ChatAction();
        action.setChatService(chatService);
        action.withServletRequest(request);
        action.withServletResponse(response);

        Logger chatActionLogger = (Logger) LoggerFactory.getLogger(ChatAction.class);
        previousChatActionLogLevel = chatActionLogger.getLevel();
        chatActionLogger.setLevel(Level.OFF);
    }

    @AfterEach
    void tearDown() {
        Logger chatActionLogger = (Logger) LoggerFactory.getLogger(ChatAction.class);
        chatActionLogger.setLevel(previousChatActionLogLevel);
        RequestContextHolder.resetRequestAttributes();
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    void deveRetornarLoginSemUsuarioAutenticado() {
        String resultado = action.exibirChat();
        assertThat(resultado).isEqualTo("login");
    }

    @Test
    void deveRetornar405QuandoEnviarMensagemComMetodoInvalido() {
        request.setUserPrincipal(() -> "admin");
        request.setMethod("GET");
        action.setChatMensagem("Ola");

        String resultado = action.enviarMensagemParcial();

        assertThat(resultado).isEqualTo("success");
        assertThat(response.getStatus()).isEqualTo(405);
        assertThat(action.getChatErro()).isNotBlank();
    }

    @Test
    void deveRetornar429QuandoRateLimitAtingido() {
        request.setUserPrincipal(() -> "admin");
        request.setMethod("POST");
        action.setChatMensagem("Ola");
        when(chatService.criarMensagem(eq("admin"), any(), any(), any(), any()))
                .thenThrow(new BusinessException(BusinessException.Code.CONFLICT, "Limite"));

        String resultado = action.enviarMensagemParcial();

        assertThat(resultado).isEqualTo("success");
        assertThat(response.getStatus()).isEqualTo(429);
        assertThat(response.getHeader("Retry-After")).isEqualTo("10");
    }

    @Test
    void deveRetornar400QuandoMensagemInvalida() {
        request.setUserPrincipal(() -> "admin");
        request.setMethod("POST");
        action.setChatMensagem("<b>x</b>");
        when(chatService.criarMensagem(eq("admin"), any(), any(), any(), any()))
                .thenThrow(new BusinessException(BusinessException.Code.INVALID_INPUT, "Mensagem inválida"));

        String resultado = action.enviarMensagemParcial();

        assertThat(resultado).isEqualTo("success");
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(action.getChatErro()).contains("Mensagem");
    }

    @Test
    void deveRetornar429QuandoPollingAtingeLimite() {
        request.setUserPrincipal(() -> "admin");
        action.setChatUltimoId(10L);
        when(chatService.buscarMensagensIncrementais("admin", 10L))
                .thenThrow(new BusinessException(BusinessException.Code.CONFLICT, "Limite de polling"));

        String resultado = action.carregarMensagensParcial();

        assertThat(resultado).isEqualTo("success");
        assertThat(response.getStatus()).isEqualTo(429);
        assertThat(response.getHeader("Retry-After")).isEqualTo("10");
    }

    @Test
    void deveRetornar500ComMensagemFallbackQuandoServicoFalhaNoEnvio() {
        request.setUserPrincipal(() -> "admin");
        request.setMethod("POST");
        action.setChatMensagem("Ola");
        when(chatService.criarMensagem(eq("admin"), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Falha inesperada"));

        String resultado = action.enviarMensagemParcial();

        assertThat(resultado).isEqualTo("success");
        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(action.getChatErro()).isEqualTo("Falha ao enviar mensagem. Tente novamente.");
    }

    @Test
    void deveRetornar500ComMensagemFallbackQuandoServicoFalhaNoPolling() {
        request.setUserPrincipal(() -> "admin");
        action.setChatUltimoId(10L);
        when(chatService.buscarMensagensIncrementais("admin", 10L))
                .thenThrow(new RuntimeException("Falha inesperada"));

        String resultado = action.carregarMensagensParcial();

        assertThat(resultado).isEqualTo("success");
        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(action.getChatErro()).isEqualTo("Não foi possível atualizar as mensagens agora.");
    }

    @Test
    void deveRetornarLoginNoEnvioParcialSemUsuarioAutenticado() {
        request.setMethod("POST");
        action.setChatMensagem("Ola");

        String resultado = action.enviarMensagemParcial();

        assertThat(resultado).isEqualTo("login");
    }

    @Test
    void deveRetornarLoginNoPollingParcialSemUsuarioAutenticado() {
        action.setChatUltimoId(10L);

        String resultado = action.carregarMensagensParcial();

        assertThat(resultado).isEqualTo("login");
    }

    @Test
    void deveCarregarMensagensIniciaisComMaiorId() {
        request.setUserPrincipal(() -> "admin");
        when(chatService.buscarHistoricoMencoes("admin", 10))
                .thenReturn(List.of(new MentionNotification("outro", "Outro", 99L, "historico")));
        when(chatService.contarMencoesPendentes("admin")).thenReturn(2);
        when(chatService.buscarMensagensIniciais("admin"))
                .thenReturn(List.of(
                        new ChatMensagemView(10L, "admin", "Admin", "A", new Date(), true),
                        new ChatMensagemView(12L, "user", "User", "B", new Date(), false)
                ));
        when(chatService.buscarParticipantesOnline()).thenReturn(List.of("Admin"));

        String resultado = action.exibirChat();

        assertThat(resultado).isEqualTo("success");
        assertThat(action.getMensagensChat()).hasSize(2);
        assertThat(action.getChatUltimoId()).isEqualTo(12L);
        assertThat(action.getParticipantesOnlineChat()).containsExactly("Admin");
        assertThat(action.getHistoricoMencoes()).hasSize(1);
        assertThat(action.getChatMencoesPendentes()).isEqualTo(2);
        verify(chatService).contarMencoesPendentes("admin");
    }

    @Test
    void deveBuscarNotificacoesDeMencoesParaUsuarioLogado() {
        request.setUserPrincipal(() -> "admin");
        when(chatService.buscarMencoesPendentes("admin"))
                .thenReturn(List.of(new com.opendev.bolao.service.dto.MentionNotification("outro", "Outro", 1L, "Teste")));

        String resultado = action.verificarMencoesPartial();

        assertThat(resultado).isEqualTo("success");
        assertThat(action.getNotificacoesMencao()).hasSize(1);
        assertThat(action.getNotificacoesMencao().get(0).getAutorNomeExibicao()).isEqualTo("Outro");
    }

    @Test
    void deveRetornarLoginNaConsultaDeMencoesSemUsuarioAutenticado() {
        String resultado = action.verificarMencoesPartial();

        assertThat(resultado).isEqualTo("login");
    }

    @Test
    void deveRetornar204QuandoNaoExistemMencoesPendentes() {
        request.setUserPrincipal(() -> "admin");
        when(chatService.buscarMencoesPendentes("admin")).thenReturn(List.of());

        String resultado = action.verificarMencoesPartial();

        assertThat(resultado).isEqualTo("none");
        assertThat(response.getStatus()).isEqualTo(204);
        assertThat(action.getNotificacoesMencao()).isEmpty();
    }

    @Test
    void deveRetornar500ComMensagemFallbackQuandoFalhaNaConsultaDeMencoes() {
        request.setUserPrincipal(() -> "admin");
        when(chatService.buscarMencoesPendentes("admin"))
                .thenThrow(new RuntimeException("Falha inesperada"));

        String resultado = action.verificarMencoesPartial();

        assertThat(resultado).isEqualTo("success");
        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(action.getChatErro()).isEqualTo("Não foi possível atualizar as notificações agora.");
    }

    @Test
    void deveRetornarBadgeComContagemDeMencoesPendentes() {
        request.setUserPrincipal(() -> "admin");
        when(chatService.contarMencoesPendentes("admin")).thenReturn(3);

        String resultado = action.verificarMencoesBadgePartial();

        assertThat(resultado).isEqualTo("success");
        assertThat(action.getChatMencoesPendentes()).isEqualTo(3);
    }

    @Test
    void deveRetornar204QuandoBadgeNaoTemMencoesPendentes() {
        request.setUserPrincipal(() -> "admin");
        when(chatService.contarMencoesPendentes("admin")).thenReturn(0);

        String resultado = action.verificarMencoesBadgePartial();

        assertThat(resultado).isEqualTo("none");
        assertThat(response.getStatus()).isEqualTo(204);
        assertThat(action.getChatMencoesPendentes()).isZero();
    }

    @Test
    void deveRetornar500ComMensagemFallbackQuandoFalhaNoBadgeDeMencoes() {
        request.setUserPrincipal(() -> "admin");
        when(chatService.contarMencoesPendentes("admin"))
                .thenThrow(new RuntimeException("Falha inesperada"));

        String resultado = action.verificarMencoesBadgePartial();

        assertThat(resultado).isEqualTo("success");
        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(action.getChatErro()).isEqualTo("Não foi possível atualizar as notificações agora.");
    }

    @Test
    void deveRetornarLoginNoBadgeParcialSemUsuarioAutenticado() {
        String resultado = action.verificarMencoesBadgePartial();

        assertThat(resultado).isEqualTo("login");
    }

    @Test
    void deveRetornarLoginNoAckDeMencoesSemUsuarioAutenticado() {
        request.setMethod("POST");
        action.setChatMencoesAckIds("1,2");

        String resultado = action.confirmarMencoesParcial();

        assertThat(resultado).isEqualTo("login");
    }

    @Test
    void deveRetornar405QuandoAckDeMencoesUsarMetodoInvalido() {
        request.setUserPrincipal(() -> "admin");
        request.setMethod("GET");
        action.setChatMencoesAckIds("1,2");

        String resultado = action.confirmarMencoesParcial();

        assertThat(resultado).isEqualTo("success");
        assertThat(response.getStatus()).isEqualTo(405);
        assertThat(action.getChatErro()).isNotBlank();
    }

    @Test
    void deveConfirmarAckDeMencoesPorIdsComSucesso() {
        request.setUserPrincipal(() -> "admin");
        request.setMethod("POST");
        request.addHeader("X-Requested-With", "XMLHttpRequest");
        action.setChatMencoesAckIds("10, 20,abc, -1");
        when(chatService.contarMencoesPendentes("admin")).thenReturn(0);

        String resultado = action.confirmarMencoesParcial();

        assertThat(resultado).isEqualTo("none");
        assertThat(response.getStatus()).isEqualTo(204);
        verify(chatService).confirmarMencoesPendentes("admin", List.of(10L, 20L));
        verify(chatService).contarMencoesPendentes("admin");
    }

    @Test
    void devePermitirAckIdempotenteSemIds() {
        request.setUserPrincipal(() -> "admin");
        request.setMethod("POST");
        request.addHeader("HX-Request", "true");
        action.setChatMencoesAckIds(" ");
        when(chatService.contarMencoesPendentes("admin")).thenReturn(3);

        String resultado = action.confirmarMencoesParcial();

        assertThat(resultado).isEqualTo("none");
        assertThat(response.getStatus()).isEqualTo(204);
        verify(chatService).confirmarMencoesPendentes("admin", List.of());
    }

    @Test
    void deveRetornar403QuandoAckNaoForAjax() {
        request.setUserPrincipal(() -> "admin");
        request.setMethod("POST");
        action.setChatMencoesAckIds("1,2");

        String resultado = action.confirmarMencoesParcial();

        assertThat(resultado).isEqualTo("success");
        assertThat(response.getStatus()).isEqualTo(403);
        verify(chatService, Mockito.never()).confirmarMencoesPendentes(any(), any());
    }

    @Test
    void deveExporHeadersDeSinalizacaoRuntimeDasMencoes() {
        request.setUserPrincipal(() -> "admin");
        when(chatService.isMencoesColdStartAtivo()).thenReturn(true);
        when(chatService.isMencoesModoDegradado()).thenReturn(true);
        when(chatService.buscarMencoesPendentes("admin")).thenReturn(List.of(
                new MentionNotification("outro", "Outro", 42L, "Teste")));

        String resultado = action.verificarMencoesPartial();

        assertThat(resultado).isEqualTo("success");
        assertThat(response.getHeader("X-Chat-Mentions-Cold-Start")).isEqualTo("true");
        assertThat(response.getHeader("X-Chat-Mentions-Degraded")).isEqualTo("true");
        assertThat(response.getHeader("X-Chat-Mentions-Delivery-Mode")).isEqualTo("memory-local-ephemeral");
        assertThat(action.isChatMencoesColdStartAtivo()).isTrue();
        assertThat(action.isChatMencoesModoDegradado()).isTrue();
    }

    @Test
    void deveRepassarReplyToMensagemIdNoEnvio() {
        request.setUserPrincipal(() -> "admin");
        request.setMethod("POST");
        action.setChatMensagem("Resposta");
        action.setChatReplyMensagemId(42L);
        when(chatService.criarMensagem(eq("admin"), any(), any(), any(), eq(42L)))
                .thenReturn(new ChatMensagemView(100L, "admin", "Admin", "Resposta", new Date(), true));
        when(chatService.buscarParticipantesOnline()).thenReturn(List.of("Admin"));

        String resultado = action.enviarMensagemParcial();

        assertThat(resultado).isEqualTo("success");
        verify(chatService).criarMensagem(eq("admin"), any(), eq("Resposta"), any(), eq(42L));
    }

    @Test
    void deveConsultarHistoricoParcialComAjax() {
        request.setUserPrincipal(() -> "admin");
        request.setMethod("GET");
        request.addHeader("HX-Request", "true");
        action.setChatBuscaTermo("gol");
        action.setChatBuscaAutor("admin");
        action.setChatBuscaDataInicio("2026-06-01");
        action.setChatBuscaDataFim("2026-06-29");
        when(chatService.buscarHistoricoFiltrado(eq("admin"), eq("gol"), eq("admin"), any(), any(), isNull(), eq(26)))
                .thenReturn(List.of(new ChatMensagemView(1L, "admin", "Admin", "gol do brasil", new Date(), true)));

        String resultado = action.consultarHistoricoParcial();

        assertThat(resultado).isEqualTo("success");
        assertThat(action.getMensagensConsulta()).hasSize(1);
    }

    @Test
    void deveManterRenderizacaoDoFragmentoQuandoConsultaNaoTemResultados() {
        request.setUserPrincipal(() -> "admin");
        request.setMethod("GET");
        request.addHeader("HX-Request", "true");
        action.setChatBuscaTermo("inexistente");
        when(chatService.buscarHistoricoFiltrado(eq("admin"), eq("inexistente"), isNull(), any(), any(), isNull(), eq(26)))
                .thenReturn(List.of());

        String resultado = action.consultarHistoricoParcial();

        assertThat(resultado).isEqualTo("success");
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(action.getMensagensConsulta()).isEmpty();
    }

    @Test
    void deveRetornar403QuandoConsultaHistoricoNaoForAjax() {
        request.setUserPrincipal(() -> "admin");
        request.setMethod("GET");
        action.setChatBuscaTermo("gol");

        String resultado = action.consultarHistoricoParcial();

        assertThat(resultado).isEqualTo("success");
        assertThat(response.getStatus()).isEqualTo(403);
    }

    @Test
    void deveRetornar405QuandoConsultaHistoricoUsarMetodoInvalido() {
        request.setUserPrincipal(() -> "admin");
        request.setMethod("POST");
        request.addHeader("HX-Request", "true");

        String resultado = action.consultarHistoricoParcial();

        assertThat(resultado).isEqualTo("success");
        assertThat(response.getStatus()).isEqualTo(405);
    }

    @Test
    void deveRetornar400QuandoDataConsultaForInvalida() {
        request.setUserPrincipal(() -> "admin");
        request.setMethod("GET");
        request.addHeader("HX-Request", "true");
        action.setChatBuscaDataInicio("2026-99-99");

        String resultado = action.consultarHistoricoParcial();

        assertThat(resultado).isEqualTo("success");
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(action.getChatErro()).isEqualTo("Data inválida no filtro de consulta.");
    }

    @Test
    void deveRetornar400QuandoCursorConsultaForInvalido() {
        request.setUserPrincipal(() -> "admin");
        request.setMethod("GET");
        request.addHeader("HX-Request", "true");
        action.setChatBuscaCursorId("abc");

        String resultado = action.consultarHistoricoParcial();

        assertThat(resultado).isEqualTo("success");
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(action.getChatErro()).isEqualTo("Cursor inválido no histórico.");
    }

    @Test
    void deveExporProximoCursorQuandoConsultaTemMaisPaginas() {
        request.setUserPrincipal(() -> "admin");
        request.setMethod("GET");
        request.addHeader("HX-Request", "true");
        when(chatService.buscarHistoricoFiltrado(eq("admin"), isNull(), isNull(), isNull(), isNull(), isNull(), eq(26)))
                .thenReturn(List.of(
                        new ChatMensagemView(10L, "u1", "U1", "m1", new Date(), false),
                        new ChatMensagemView(11L, "u1", "U1", "m2", new Date(), false),
                        new ChatMensagemView(12L, "u1", "U1", "m3", new Date(), false),
                        new ChatMensagemView(13L, "u1", "U1", "m4", new Date(), false),
                        new ChatMensagemView(14L, "u1", "U1", "m5", new Date(), false),
                        new ChatMensagemView(15L, "u1", "U1", "m6", new Date(), false),
                        new ChatMensagemView(16L, "u1", "U1", "m7", new Date(), false),
                        new ChatMensagemView(17L, "u1", "U1", "m8", new Date(), false),
                        new ChatMensagemView(18L, "u1", "U1", "m9", new Date(), false),
                        new ChatMensagemView(19L, "u1", "U1", "m10", new Date(), false),
                        new ChatMensagemView(20L, "u1", "U1", "m11", new Date(), false),
                        new ChatMensagemView(21L, "u1", "U1", "m12", new Date(), false),
                        new ChatMensagemView(22L, "u1", "U1", "m13", new Date(), false),
                        new ChatMensagemView(23L, "u1", "U1", "m14", new Date(), false),
                        new ChatMensagemView(24L, "u1", "U1", "m15", new Date(), false),
                        new ChatMensagemView(25L, "u1", "U1", "m16", new Date(), false),
                        new ChatMensagemView(26L, "u1", "U1", "m17", new Date(), false),
                        new ChatMensagemView(27L, "u1", "U1", "m18", new Date(), false),
                        new ChatMensagemView(28L, "u1", "U1", "m19", new Date(), false),
                        new ChatMensagemView(29L, "u1", "U1", "m20", new Date(), false),
                        new ChatMensagemView(30L, "u1", "U1", "m21", new Date(), false),
                        new ChatMensagemView(31L, "u1", "U1", "m22", new Date(), false),
                        new ChatMensagemView(32L, "u1", "U1", "m23", new Date(), false),
                        new ChatMensagemView(33L, "u1", "U1", "m24", new Date(), false),
                        new ChatMensagemView(34L, "u1", "U1", "m25", new Date(), false),
                        new ChatMensagemView(35L, "u1", "U1", "m26", new Date(), false)
                ));

        String resultado = action.consultarHistoricoParcial();

        assertThat(resultado).isEqualTo("success");
        assertThat(action.getMensagensConsulta()).hasSize(25);
        assertThat(action.isChatBuscaTemMais()).isTrue();
        assertThat(action.getChatBuscaProximoCursorId()).isEqualTo(11L);
    }
}
