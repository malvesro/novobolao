package com.opendev.bolao.service.impl;

import com.opendev.bolao.model.ChatMencao;
import com.opendev.bolao.repository.ChatMencaoRepository;
import com.opendev.bolao.service.ChatNotificationService;
import com.opendev.bolao.service.dto.MentionNotification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatNotificationServiceImplTest {

    @Mock
    private ChatMencaoRepository repository;

    private ChatNotificationService service;

    @BeforeEach
    void setUp() {
        ChatNotificationServiceImpl impl = new ChatNotificationServiceImpl();
        impl.setChatMencaoRepository(repository);
        service = impl;
    }

    @Test
    void deveRegistrarELerMencoesPendentesSemConsumirNoGetNoModoPersistente() {
        when(repository.save(any(ChatMencao.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(repository.countByDestinatarioLoginAndDataConfirmacaoIsNull("user")).thenReturn(1L);
        when(repository.countByDestinatarioLogin("user")).thenReturn(1L);
        when(repository.findByDestinatarioLoginAndDataConfirmacaoIsNullOrderByIdAsc(eq("user"), any(Pageable.class)))
                .thenReturn(List.of(criarMencao("user", "admin", "Administrador", 1L, "Ola @user", true)));

        service.registrarMencoes("admin", "Administrador", "Ola @user", 1L, Set.of("user"));
        List<MentionNotification> primeiraLeitura = service.buscarMencoesPendentes("user");
        List<MentionNotification> segundaLeitura = service.buscarMencoesPendentes("user");

        assertThat(primeiraLeitura).hasSize(1);
        assertThat(primeiraLeitura.get(0).getAutorLogin()).isEqualTo("admin");
        assertThat(primeiraLeitura.get(0).getMensagemPreview()).isEqualTo("Ola @user");
        assertThat(segundaLeitura).hasSize(1);
        assertThat(service.contarMencoesPendentes("user")).isEqualTo(1);
    }

    @Test
    void deveConfirmarAckPorIdsComIdempotenciaNoModoPersistente() {
        ChatMencao m2 = criarMencao("user", "admin", "Administrador", 2L, "msg-2", true);
        ChatMencao m3 = criarMencao("user", "admin", "Administrador", 3L, "msg-3", true);
        when(repository.findByDestinatarioLoginAndChatMensagemIdIn("user", Set.of(2L, 3L)))
                .thenReturn(List.of(m2, m3))
                .thenReturn(List.of(m2, m3));
        when(repository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(repository.countByDestinatarioLoginAndDataConfirmacaoIsNull("user")).thenReturn(0L);

        int removidasPrimeiroAck = service.confirmarMencoesPendentes("user", Set.of(2L, 3L));
        int removidasSegundoAck = service.confirmarMencoesPendentes("user", Set.of(2L, 3L));

        assertThat(removidasPrimeiroAck).isEqualTo(2);
        assertThat(removidasSegundoAck).isZero();
    }

    @Test
    void devePersistirHistoricoRecenteNoModoPersistente() {
        ChatMencao rec1 = criarMencao("user", "admin", "Administrador", 120L, "hist-120", false);
        ChatMencao rec2 = criarMencao("user", "admin", "Administrador", 119L, "hist-119", false);
        when(repository.findByDestinatarioLoginOrderByIdDesc(eq("user"), any(Pageable.class)))
                .thenReturn(List.of(rec1, rec2));

        List<MentionNotification> historico = service.buscarHistoricoMencoes("user", 10);

        assertThat(historico).hasSize(2);
        assertThat(historico.get(0).getMensagemPreview()).isEqualTo("hist-120");
        assertThat(historico.get(1).getMensagemPreview()).isEqualTo("hist-119");
    }

    @Test
    void deveAtivarFallbackMemoriaQuandoRepositorioFalha() {
        when(repository.save(any(ChatMencao.class)))
                .thenThrow(new RuntimeException("db offline"));

        service.registrarMencoes("admin", "Administrador", "oi @user", 10L, Set.of("user"));
        assertThat(service.isModoMemoriaLocal()).isTrue();

        List<MentionNotification> pendentes = service.buscarMencoesPendentes("user");
        assertThat(pendentes).hasSize(1);
        assertThat(pendentes.get(0).getChatMensagemId()).isEqualTo(10L);

        int ack = service.confirmarMencoesPendentes("user", Set.of(10L));
        assertThat(ack).isEqualTo(1);
        assertThat(service.contarMencoesPendentes("user")).isZero();
    }

    @Test
    void devePersistirDestinatarioNormalizadoComUnicidadePorMensagem() {
        when(repository.save(any(ChatMencao.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(repository.countByDestinatarioLoginAndDataConfirmacaoIsNull("user")).thenReturn(1L);
        when(repository.countByDestinatarioLogin("user")).thenReturn(1L);

        service.registrarMencoes("admin", "Administrador", "msg", 77L, Set.of(" USER "));

        ArgumentCaptor<ChatMencao> captor = ArgumentCaptor.forClass(ChatMencao.class);
        verify(repository).save(captor.capture());
        ChatMencao salvo = captor.getValue();
        assertThat(salvo.getDestinatarioLogin()).isEqualTo("user");
        assertThat(salvo.getChatMensagemId()).isEqualTo(77L);
        assertThat(salvo.getDataConfirmacao()).isNull();
    }

    @Test
    void deveExporSinalizacaoDeModoEColdStartNoModoPersistente() {
        assertThat(service.isModoMemoriaLocal()).isFalse();
        assertThat(service.isColdStartAtivo()).isTrue();
    }

    @Test
    void deveRecuperarAutomaticamenteFallbackParaPersistenciaComReplaySeguro() {
        AtomicBoolean primeiraFalhaMensagemUm = new AtomicBoolean(true);
        when(repository.save(any(ChatMencao.class))).thenAnswer(invocation -> {
            ChatMencao mencao = invocation.getArgument(0);
            if (Long.valueOf(1L).equals(mencao.getChatMensagemId()) && primeiraFalhaMensagemUm.getAndSet(false)) {
                throw new RuntimeException("db offline");
            }
            return mencao;
        });
        when(repository.countByDestinatarioLoginAndDataConfirmacaoIsNull(any(String.class))).thenReturn(1L);
        when(repository.countByDestinatarioLogin("user")).thenReturn(1L);

        service.registrarMencoes("admin", "Administrador", "oi @user", 1L, Set.of("user"));
        assertThat(service.isModoMemoriaLocal()).isTrue();
        assertThat(service.buscarMencoesPendentes("user")).hasSize(1);

        service.registrarMencoes("admin", "Administrador", "oi de novo @user", 2L, Set.of("user"));
        assertThat(service.isModoMemoriaLocal()).isFalse();

        ArgumentCaptor<ChatMencao> captor = ArgumentCaptor.forClass(ChatMencao.class);
        verify(repository, atLeastOnce()).save(captor.capture());
        long totalMensagem1 = captor.getAllValues().stream()
                .filter(item -> Long.valueOf(1L).equals(item.getChatMensagemId()))
                .count();
        long totalMensagem2 = captor.getAllValues().stream()
                .filter(item -> Long.valueOf(2L).equals(item.getChatMensagemId()))
                .count();
        assertThat(totalMensagem1).isGreaterThanOrEqualTo(2L);
        assertThat(totalMensagem2).isEqualTo(1L);
    }

    @Test
    void deveTratarDuplicidadeDeCorridaComoIdempotenteSemAtivarFallback() {
        when(repository.save(any(ChatMencao.class)))
                .thenThrow(new DataIntegrityViolationException(
                        "duplicate key value violates unique constraint UK_CHT_MENCAO_DEST_MSG"));

        service.registrarMencoes("admin", "Administrador", "oi @user", 99L, Set.of("user"));

        assertThat(service.isModoMemoriaLocal()).isFalse();
    }

    private ChatMencao criarMencao(String destinatarioLogin,
                                   String autorLogin,
                                   String autorNome,
                                   Long chatMensagemId,
                                   String preview,
                                   boolean pendente) {
        ChatMencao item = new ChatMencao();
        item.setDestinatarioLogin(destinatarioLogin);
        item.setAutorLogin(autorLogin);
        item.setAutorNomeExibicao(autorNome);
        item.setChatMensagemId(chatMensagemId);
        item.setMensagemPreview(preview);
        item.setDataCriacao(new Date());
        if (!pendente) {
            item.setDataConfirmacao(new Date());
        }
        return item;
    }
}
