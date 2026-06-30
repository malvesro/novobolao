package com.opendev.bolao.service.impl;

import com.opendev.bolao.exception.BusinessException;
import com.opendev.bolao.model.ChatMensagem;
import com.opendev.bolao.model.Participante;
import com.opendev.bolao.repository.ChatMensagemRepository;
import com.opendev.bolao.repository.ParticipanteRepository;
import com.opendev.bolao.service.ChatNotificationService;
import com.opendev.bolao.service.dto.ChatMensagemView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceImplTest {

    @Mock
    private ChatMensagemRepository chatMensagemRepository;

    @Mock
    private ParticipanteRepository participanteRepository;

    @Mock
    private ChatNotificationService chatNotificationService;

    @InjectMocks
    private ChatServiceImpl chatService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void deveOrdenarMensagensIniciaisEmOrdemCrescente() {
        ChatMensagem m2 = criarMensagem(2L, "admin", "Mensagem 2");
        ChatMensagem m1 = criarMensagem(1L, "user", "Mensagem 1");
        when(chatMensagemRepository.findAllByOrderByIdDesc(any(Pageable.class))).thenReturn(List.of(m2, m1));

        List<ChatMensagemView> resultado = chatService.buscarMensagensIniciais("admin");

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getId()).isEqualTo(1L);
        assertThat(resultado.get(1).getId()).isEqualTo(2L);
    }

    @Test
    void deveRejeitarMensagemComHtml() {
        assertThatThrownBy(() -> chatService.criarMensagem("admin", "sessao-admin", "<script>alert(1)</script>", "127.0.0.1"))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(BusinessException.Code.INVALID_INPUT);
    }

    @Test
    void deveAplicarRateLimitNoEnvio() {
        stubParticipanteAdmin();
        when(chatMensagemRepository.save(any(ChatMensagem.class))).thenAnswer(invocation -> {
            ChatMensagem mensagem = invocation.getArgument(0);
            mensagem.setId(1L);
            return mensagem;
        });

        for (int i = 0; i < 10; i++) {
            chatService.criarMensagem("admin", "sessao-admin", "Mensagem " + i, "127.0.0.1");
        }

        assertThatThrownBy(() -> chatService.criarMensagem("admin", "sessao-admin", "Mensagem 11", "127.0.0.1"))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(BusinessException.Code.CONFLICT);
    }

    @Test
    void deveRetornarSomenteIncrementaisPorUltimoId() {
        ChatMensagem m3 = criarMensagem(3L, "user", "Nova 1");
        ChatMensagem m4 = criarMensagem(4L, "admin", "Nova 2");
        when(chatMensagemRepository.findByIdGreaterThanOrderByIdAsc(any(Long.class), any(Pageable.class)))
                .thenReturn(List.of(m3, m4));

        List<ChatMensagemView> resultado = chatService.buscarMensagensIncrementais("admin", 2L);

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).isAutoriaDoUsuarioAtual()).isFalse();
        assertThat(resultado.get(1).isAutoriaDoUsuarioAtual()).isTrue();
    }

    @Test
    void deveRejeitarMensagemVazia() {
        assertThatThrownBy(() -> chatService.criarMensagem("admin", "sessao-admin", "   ", "127.0.0.1"))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(BusinessException.Code.INVALID_INPUT);
    }

    @Test
    void deveAplicarRateLimitNoPollingIncremental() {
        when(chatMensagemRepository.findByIdGreaterThanOrderByIdAsc(any(Long.class), any(Pageable.class)))
                .thenReturn(List.of());

        for (int i = 0; i < 30; i++) {
            List<ChatMensagemView> resultado = chatService.buscarMensagensIncrementais("admin", 0L);
            assertThat(resultado).isEmpty();
        }

        assertThatThrownBy(() -> chatService.buscarMensagensIncrementais("admin", 0L))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(BusinessException.Code.CONFLICT);
    }

    @Test
    void deveAceitarMensagemComLimiteExatoDe300Caracteres() {
        stubParticipanteAdmin();
        String texto300 = "a".repeat(300);
        when(chatMensagemRepository.save(any(ChatMensagem.class))).thenAnswer(invocation -> {
            ChatMensagem mensagem = invocation.getArgument(0);
            mensagem.setId(1L);
            return mensagem;
        });

        ChatMensagemView resultado = chatService.criarMensagem("admin", "sessao-admin", texto300, "127.0.0.1");

        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getTexto()).hasSize(300);
        assertThat(resultado.getTexto()).isEqualTo(texto300);
    }

    @Test
    void deveUsarNomeDoParticipanteComoNomeDeExibicao() {
        stubParticipanteAdmin();
        when(chatMensagemRepository.save(any(ChatMensagem.class))).thenAnswer(invocation -> {
            ChatMensagem mensagem = invocation.getArgument(0);
            mensagem.setId(1L);
            return mensagem;
        });

        ChatMensagemView resultado = chatService.criarMensagem("admin", "sessao-admin", "Mensagem válida", "127.0.0.1");

        assertThat(resultado.getNomeExibicao()).isEqualTo("Administrador");
    }

    @Test
    void deveRegistrarNotificacaoParaUsuarioMencionadoSemDependenciaDePresencaNoChat() {
        stubParticipanteAdmin();
        when(participanteRepository.existsByLoginAndHabilitadoTrue("amigo")).thenReturn(true);
        when(chatMensagemRepository.save(any(ChatMensagem.class))).thenAnswer(invocation -> {
            ChatMensagem mensagem = invocation.getArgument(0);
            mensagem.setId(1L);
            return mensagem;
        });

        chatService.criarMensagem("admin", "sessao-admin", "Olá @amigo", "127.0.0.1");

        verify(chatNotificationService).registrarMencoes(eq("admin"), eq("Administrador"), eq("Olá @amigo"), eq(1L), argThat(destinatarios -> destinatarios.contains("amigo") && destinatarios.size() == 1));
    }

    @Test
    void deveRegistrarNotificacaoParaTodosUsuariosHabilitadosForaDaTelaDeChat() {
        stubParticipanteAdmin();
        Participante outro = new Participante();
        outro.setLogin("outro");
        when(participanteRepository.findAllByHabilitadoTrueAndLoginNot("admin"))
                .thenReturn(List.of(outro));
        when(chatMensagemRepository.save(any(ChatMensagem.class))).thenAnswer(invocation -> {
            ChatMensagem mensagem = invocation.getArgument(0);
            mensagem.setId(1L);
            return mensagem;
        });

        chatService.criarMensagem("admin", "sessao-admin", "@Todos bom dia", "127.0.0.1");

        verify(chatNotificationService).registrarMencoes(eq("admin"), eq("Administrador"), eq("@Todos bom dia"), eq(1L), argThat(destinatarios -> destinatarios.contains("outro") && destinatarios.size() == 1));
    }

    @Test
    void naoDeveNotificarMencaoDiretaParaLoginInexistenteOuDesabilitado() {
        stubParticipanteAdmin();
        when(participanteRepository.existsByLoginAndHabilitadoTrue("inativo")).thenReturn(false);
        when(chatMensagemRepository.save(any(ChatMensagem.class))).thenAnswer(invocation -> {
            ChatMensagem mensagem = invocation.getArgument(0);
            mensagem.setId(1L);
            return mensagem;
        });

        chatService.criarMensagem("admin", "sessao-admin", "Olá @inativo", "127.0.0.1");

        verify(chatNotificationService).registrarMencoes(eq("admin"), eq("Administrador"), eq("Olá @inativo"), eq(1L),
                argThat(Set::isEmpty));
    }

    @Test
    void deveUsarLoginComoNomeDeExibicaoCasoParticipanteNaoTenhaNome() {
        Participante p = new Participante();
        p.setNome(null);
        when(participanteRepository.findByLogin("user")).thenReturn(Optional.of(p));
        when(chatMensagemRepository.save(any(ChatMensagem.class))).thenAnswer(invocation -> {
            ChatMensagem mensagem = invocation.getArgument(0);
            mensagem.setId(1L);
            return mensagem;
        });

        ChatMensagemView resultado = chatService.criarMensagem("user", "sessao-user", "Olá", "127.0.0.1");

        assertThat(resultado.getNomeExibicao()).isEqualTo("user");
    }

    @Test
    void deveTratarUltimoIdNuloComoZeroNoPolling() {
        when(chatMensagemRepository.findByIdGreaterThanOrderByIdAsc(any(Long.class), any(Pageable.class)))
                .thenReturn(List.of());

        List<ChatMensagemView> resultado = chatService.buscarMensagensIncrementais("admin", null);

        assertThat(resultado).isEmpty();
        verify(chatMensagemRepository).findByIdGreaterThanOrderByIdAsc(eq(0L), any(Pageable.class));
    }

    @Test
    void deveTratarUltimoIdNegativoComoZeroNoPolling() {
        when(chatMensagemRepository.findByIdGreaterThanOrderByIdAsc(any(Long.class), any(Pageable.class)))
                .thenReturn(List.of());

        List<ChatMensagemView> resultado = chatService.buscarMensagensIncrementais("admin", -5L);

        assertThat(resultado).isEmpty();
        verify(chatMensagemRepository).findByIdGreaterThanOrderByIdAsc(eq(0L), any(Pageable.class));
    }

    @Test
    void devePropagarFalhaDeRepositorioAoCriarMensagem() {
        stubParticipanteAdmin();
        when(chatMensagemRepository.save(any(ChatMensagem.class)))
                .thenThrow(new RuntimeException("Falha no banco"));

        assertThatThrownBy(() -> chatService.criarMensagem("admin", "sessao-admin", "Mensagem válida", "127.0.0.1"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Falha no banco");
    }

    @Test
    void deveDocumentarComportamentoAtualTruncandoMensagemAcimaDoLimite() {
        stubParticipanteAdmin();
        String texto301 = "a".repeat(301);
        when(chatMensagemRepository.save(any(ChatMensagem.class))).thenAnswer(invocation -> {
            ChatMensagem mensagem = invocation.getArgument(0);
            mensagem.setId(1L);
            return mensagem;
        });

        ChatMensagemView resultado = chatService.criarMensagem("admin", "sessao-admin", texto301, "127.0.0.1");

        assertThat(resultado.getTexto()).hasSize(300);
        assertThat(resultado.getTexto()).isEqualTo(texto301.substring(0, 300));
    }

    @Test
    void deveContarMencoesPendentesViaServicoDeNotificacao() {
        when(chatNotificationService.contarMencoesPendentes("admin")).thenReturn(4);

        int total = chatService.contarMencoesPendentes("admin");

        assertThat(total).isEqualTo(4);
        verify(chatNotificationService).contarMencoesPendentes("admin");
    }

    @Test
    void deveRegistrarMensagemComoRespostaQuandoReplyToValido() {
        stubParticipanteAdmin();
        when(chatMensagemRepository.existsById(42L)).thenReturn(true);
        when(chatMensagemRepository.save(any(ChatMensagem.class))).thenAnswer(invocation -> {
            ChatMensagem mensagem = invocation.getArgument(0);
            mensagem.setId(100L);
            return mensagem;
        });

        ChatMensagemView resultado = chatService.criarMensagem(
                "admin", "sessao-admin", "Respondendo", "127.0.0.1", 42L);

        assertThat(resultado.getId()).isEqualTo(100L);
        verify(chatMensagemRepository).save(argThat(m -> m.getReplyToMensagemId() != null && m.getReplyToMensagemId().equals(42L)));
    }

    @Test
    void deveRejeitarReplyToInexistente() {
        when(chatMensagemRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> chatService.criarMensagem(
                "admin", "sessao-admin", "Oi", "127.0.0.1", 999L))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(BusinessException.Code.INVALID_INPUT);
    }

    @Test
    void deveMapearContextoDaMensagemPaiNoIncremental() {
        ChatMensagem resposta = criarMensagem(11L, "admin", "Resposta");
        resposta.setReplyToMensagemId(10L);
        ChatMensagem mensagemPai = criarMensagem(10L, "user", "Mensagem original de referência");
        when(chatMensagemRepository.findByIdGreaterThanOrderByIdAsc(any(Long.class), any(Pageable.class)))
                .thenReturn(List.of(resposta));
        when(chatMensagemRepository.findAllById(argThat(ids -> {
            for (Long id : ids) {
                if (Long.valueOf(10L).equals(id)) {
                    return true;
                }
            }
            return false;
        })))
                .thenReturn(List.of(mensagemPai));

        List<ChatMensagemView> resultado = chatService.buscarMensagensIncrementais("admin", 5L);

        assertThat(resultado).hasSize(1);
        ChatMensagemView view = resultado.get(0);
        assertThat(view.getReplyToMensagemId()).isEqualTo(10L);
        assertThat(view.getReplyToNomeExibicao()).isEqualTo("user");
        assertThat(view.getReplyToTextoPreview()).contains("Mensagem original");
    }

    @Test
    void deveConsultarHistoricoComFiltros() {
        ChatMensagem mensagem = criarMensagem(50L, "admin", "Busca por termo");
        when(chatMensagemRepository.buscarHistoricoFiltrado(eq("termo"), eq("admin"), any(), any(), isNull(), any()))
                .thenReturn(new PageImpl<>(List.of(mensagem)));

        List<ChatMensagemView> resultado = chatService.buscarHistoricoFiltrado(
                "admin", "termo", "admin", new Date(System.currentTimeMillis() - 1_000L), new Date(), null, 20);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getTexto()).contains("Busca");
    }

    @Test
    void deveConsultarHistoricoComCursorQuandoInformado() {
        ChatMensagem mensagem = criarMensagem(40L, "admin", "Mensagem antiga");
        when(chatMensagemRepository.buscarHistoricoFiltrado(eq("gol"), eq("admin"), any(), any(), eq(50L), any()))
                .thenReturn(new PageImpl<>(List.of(mensagem)));

        List<ChatMensagemView> resultado = chatService.buscarHistoricoFiltrado(
                "admin", "gol", "admin", new Date(System.currentTimeMillis() - 2_000L), new Date(), 50L, 20);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getId()).isEqualTo(40L);
    }

    @Test
    void deveRejeitarCursorInvalidoNaConsultaDeHistorico() {
        assertThatThrownBy(() -> chatService.buscarHistoricoFiltrado(
                "admin", "gol", "admin", null, null, -1L, 20))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(BusinessException.Code.INVALID_INPUT);
    }

    @Test
    void deveBuscarHistoricoRecenteDeMencoesViaServicoDeNotificacao() {
        List<com.opendev.bolao.service.dto.MentionNotification> historico = List.of(
                new com.opendev.bolao.service.dto.MentionNotification("autor", "Autor", 10L, "oi @admin")
        );
        when(chatNotificationService.buscarHistoricoMencoes("admin", 10)).thenReturn(historico);

        List<com.opendev.bolao.service.dto.MentionNotification> resultado =
                chatService.buscarHistoricoMencoes("admin", 10);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getAutorLogin()).isEqualTo("autor");
        verify(chatNotificationService).buscarHistoricoMencoes("admin", 10);
    }


    private void stubParticipanteAdmin() {
        Participante p = new Participante();
        p.setNome("Administrador");
        when(participanteRepository.findByLogin("admin")).thenReturn(Optional.of(p));
    }

    private ChatMensagem criarMensagem(Long id, String login, String texto) {
        ChatMensagem mensagem = new ChatMensagem();
        mensagem.setId(id);
        mensagem.setLoginAutor(login);
        mensagem.setNomeExibicao(login);
        mensagem.setTexto(texto);
        mensagem.setDataEnvio(new Date());
        return mensagem;
    }
}
