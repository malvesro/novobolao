package com.opendev.bolao.action;

import com.opendev.bolao.exception.BusinessException;
import com.opendev.bolao.service.ChatService;
import com.opendev.bolao.service.ChatNotificationService;
import com.opendev.bolao.service.dto.ChatMensagemView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChatActionTest {

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private ChatService chatService;
    private com.opendev.bolao.service.ChatNotificationService chatNotificationService;
    private ChatAction action;

    @BeforeEach
    void setup() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, response));

        chatService = Mockito.mock(ChatService.class);
        chatNotificationService = Mockito.mock(ChatNotificationService.class);
        action = new ChatAction();
        action.setChatService(chatService);
        action.setChatNotificationService(chatNotificationService);
        action.withServletRequest(request);
        action.withServletResponse(response);
    }

    @AfterEach
    void tearDown() {
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
        when(chatService.criarMensagem(eq("admin"), any(), any(), any()))
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
        when(chatService.criarMensagem(eq("admin"), any(), any(), any()))
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
        when(chatService.criarMensagem(eq("admin"), any(), any(), any()))
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
    }

    @Test
    void deveBuscarNotificacoesDeMencoesParaUsuarioLogado() {
        request.setUserPrincipal(() -> "admin");
        when(chatNotificationService.buscarMencoesPendentes("admin"))
                .thenReturn(List.of(new com.opendev.bolao.service.dto.MentionNotification("outro", "Outro", 1L, "Teste")));

        String resultado = action.verificarMencoesPartial();

        assertThat(resultado).isEqualTo("success");
        assertThat(action.getNotificacoesMencao()).hasSize(1);
        assertThat(action.getNotificacoesMencao().get(0).getAutorNomeExibicao()).isEqualTo("Outro");
    }

    @Test
    void deveRetornar204QuandoNaoExistemMencoesPendentes() {
        request.setUserPrincipal(() -> "admin");
        when(chatNotificationService.buscarMencoesPendentes("admin")).thenReturn(List.of());

        String resultado = action.verificarMencoesPartial();

        assertThat(resultado).isEqualTo("success");
        assertThat(response.getStatus()).isEqualTo(204);
        assertThat(action.getNotificacoesMencao()).isEmpty();
    }
}
