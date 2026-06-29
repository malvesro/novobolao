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
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
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
    void deveRegistrarNotificacaoParaUsuarioMencionado() {
        stubParticipanteAdmin();
        when(participanteRepository.findByLogin("amigo")).thenReturn(Optional.of(new Participante()));
        chatService.atualizarPresenca("amigo");
        when(chatMensagemRepository.save(any(ChatMensagem.class))).thenAnswer(invocation -> {
            ChatMensagem mensagem = invocation.getArgument(0);
            mensagem.setId(1L);
            return mensagem;
        });

        chatService.criarMensagem("admin", "sessao-admin", "Olá @amigo", "127.0.0.1");

        verify(chatNotificationService).registrarMencoes(eq("admin"), eq("Administrador"), eq("Olá @amigo"), eq(1L), argThat(destinatarios -> destinatarios.contains("amigo") && destinatarios.size() == 1));
    }

    @Test
    void deveRegistrarNotificacaoParaTodosUsuariosOnline() {
        stubParticipanteAdmin();
        chatService.atualizarPresenca("outro");
        when(chatMensagemRepository.save(any(ChatMensagem.class))).thenAnswer(invocation -> {
            ChatMensagem mensagem = invocation.getArgument(0);
            mensagem.setId(1L);
            return mensagem;
        });

        chatService.criarMensagem("admin", "sessao-admin", "@Todos bom dia", "127.0.0.1");

        verify(chatNotificationService).registrarMencoes(eq("admin"), eq("Administrador"), eq("@Todos bom dia"), eq(1L), argThat(destinatarios -> destinatarios.contains("outro") && destinatarios.size() == 1));
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
