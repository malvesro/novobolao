package com.opendev.bolao.service.impl;

import com.opendev.bolao.model.ChatMencao;
import com.opendev.bolao.repository.ChatMencaoRepository;
import com.opendev.bolao.service.ChatNotificationService;
import com.opendev.bolao.service.dto.MentionNotification;
import com.opendev.bolao.util.ValidacaoUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ChatNotificationServiceImpl implements ChatNotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatNotificationServiceImpl.class);
    private static final int MAX_PENDING_NOTIFICATIONS = 50;
    private static final int MAX_HISTORY_NOTIFICATIONS = 100;
    private static final long COLD_START_WINDOW_MS = 300_000L;
    private static final long RECOVERY_BASE_BACKOFF_MS = 5_000L;
    private static final long RECOVERY_MAX_BACKOFF_MS = 60_000L;
    private static final String RECOVERY_PROBE_LOGIN = "__chat_mention_healthcheck__";
    private static final String UNIQUE_CONSTRAINT_MENCAO = "UK_CHT_MENCAO_DEST_MSG";

    private ChatMencaoRepository chatMencaoRepository;
    private final ConcurrentMap<String, Deque<MentionNotification>> mencoesPendentes = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Deque<MentionNotification>> historicoMencoes = new ConcurrentHashMap<>();
    private volatile boolean fallbackMemoriaAtivo;
    private volatile long fallbackStartedAt;
    private volatile long nextRecoveryAttemptAt;
    private volatile int recoveryFailures;
    private final long startedAt = System.currentTimeMillis();

    public ChatNotificationServiceImpl() {
        LOGGER.info("[CHAT][MENTION][MODE] mode=BOOTSTRAP status=INITIALIZING");
    }

    @Override
    public void registrarMencoes(String autorLogin,
                                 String autorNomeExibicao,
                                 String texto,
                                 Long mensagemId,
                                 Set<String> destinatarios) {
        if (destinatarios == null || destinatarios.isEmpty() || ValidacaoUtils.isVazia(autorLogin)
                || mensagemId == null || mensagemId <= 0) {
            return;
        }

        String autorLoginSeguro = autorLogin.trim().toLowerCase(Locale.ROOT);
        String mensagemPreview = buildMensagemPreview(texto);
        Set<String> destinatariosSeguros = normalizarDestinatarios(autorLoginSeguro, destinatarios);
        if (destinatariosSeguros.isEmpty()) {
            return;
        }

        if (usarPersistenciaDuravel()) {
            try {
                registrarMencoesDuravel(autorLoginSeguro, autorNomeExibicao, mensagemId, mensagemPreview, destinatariosSeguros);
                return;
            } catch (RuntimeException ex) {
                ativarFallbackMemoria("FALHA_PERSISTENCIA_REGISTRAR", ex);
            }
        }

        registrarMencoesMemoria(autorLoginSeguro, autorNomeExibicao, mensagemId, mensagemPreview, destinatariosSeguros);
    }

    @Override
    public List<MentionNotification> buscarMencoesPendentes(String loginAtual) {
        if (ValidacaoUtils.isVazia(loginAtual)) {
            return Collections.emptyList();
        }

        String loginSeguro = loginAtual.trim().toLowerCase(Locale.ROOT);
        if (usarPersistenciaDuravel()) {
            try {
                List<ChatMencao> pendencias = chatMencaoRepository.findByDestinatarioLoginAndDataConfirmacaoIsNullOrderByIdAsc(
                        loginSeguro, PageRequest.of(0, MAX_PENDING_NOTIFICATIONS));
                if (pendencias.isEmpty()) {
                    return Collections.emptyList();
                }
                LOGGER.debug("[CHAT][MENTION][BACKLOG] mode=PERSISTENT user={} pending={} status=READ_ONLY",
                        loginSeguro, pendencias.size());
                return mapearParaNotificacao(pendencias);
            } catch (RuntimeException ex) {
                ativarFallbackMemoria("FALHA_PERSISTENCIA_BACKLOG", ex);
            }
        }

        Deque<MentionNotification> fila = mencoesPendentes.computeIfAbsent(loginSeguro, key -> new ArrayDeque<>());
        synchronized (fila) {
            if (fila.isEmpty()) {
                return Collections.emptyList();
            }
            LOGGER.debug("[CHAT][MENTION][BACKLOG] mode=MEMORY_LOCAL user={} pending={} status=READ_ONLY",
                    loginSeguro, fila.size());
            return new ArrayList<>(fila);
        }
    }

    @Override
    public int confirmarMencoesPendentes(String loginAtual, Set<Long> mensagemIds) {
        if (ValidacaoUtils.isVazia(loginAtual) || mensagemIds == null || mensagemIds.isEmpty()) {
            return 0;
        }

        String loginSeguro = loginAtual.trim().toLowerCase(Locale.ROOT);
        if (usarPersistenciaDuravel()) {
            try {
                List<ChatMencao> mencoes = chatMencaoRepository.findByDestinatarioLoginAndChatMensagemIdIn(loginSeguro, mensagemIds);
                if (mencoes.isEmpty()) {
                    LOGGER.info("[CHAT][MENTION][ACK] mode=PERSISTENT user={} requestIds={} removed=0 status=EMPTY_QUEUE",
                            loginSeguro, mensagemIds.size());
                    return 0;
                }
                int removidas = 0;
                Date agora = new Date();
                for (ChatMencao mencao : mencoes) {
                    if (mencao != null && mencao.getDataConfirmacao() == null) {
                        mencao.setDataConfirmacao(agora);
                        removidas++;
                    }
                }
                if (removidas > 0) {
                    chatMencaoRepository.saveAll(mencoes);
                }
                LOGGER.info("[CHAT][MENTION][ACK] mode=PERSISTENT user={} requestIds={} removed={} pending={} status=OK",
                        loginSeguro, mensagemIds.size(), removidas,
                        chatMencaoRepository.countByDestinatarioLoginAndDataConfirmacaoIsNull(loginSeguro));
                return removidas;
            } catch (RuntimeException ex) {
                ativarFallbackMemoria("FALHA_PERSISTENCIA_ACK", ex);
            }
        }

        Deque<MentionNotification> fila = mencoesPendentes.computeIfAbsent(loginSeguro, key -> new ArrayDeque<>());
        synchronized (fila) {
            if (fila.isEmpty()) {
                LOGGER.info("[CHAT][MENTION][ACK] mode=MEMORY_LOCAL user={} requestIds={} removed=0 pending=0 status=EMPTY_QUEUE",
                        loginSeguro, mensagemIds.size());
                return 0;
            }
            int tamanhoAntes = fila.size();
            fila.removeIf(notificacao -> notificacao != null
                    && notificacao.getChatMensagemId() != null
                    && mensagemIds.contains(notificacao.getChatMensagemId()));
            int removidas = tamanhoAntes - fila.size();
            LOGGER.info("[CHAT][MENTION][ACK] mode=MEMORY_LOCAL user={} requestIds={} removed={} pending={} status=OK",
                    loginSeguro, mensagemIds.size(), removidas, fila.size());
            return removidas;
        }
    }

    @Override
    public int contarMencoesPendentes(String loginAtual) {
        if (ValidacaoUtils.isVazia(loginAtual)) {
            return 0;
        }

        String loginSeguro = loginAtual.trim().toLowerCase(Locale.ROOT);
        if (usarPersistenciaDuravel()) {
            try {
                return (int) chatMencaoRepository.countByDestinatarioLoginAndDataConfirmacaoIsNull(loginSeguro);
            } catch (RuntimeException ex) {
                ativarFallbackMemoria("FALHA_PERSISTENCIA_COUNT", ex);
            }
        }

        Deque<MentionNotification> fila = mencoesPendentes.computeIfAbsent(loginSeguro, key -> new ArrayDeque<>());
        synchronized (fila) {
            return fila.size();
        }
    }

    @Override
    public List<MentionNotification> buscarHistoricoMencoes(String loginAtual, int limite) {
        if (ValidacaoUtils.isVazia(loginAtual) || limite <= 0) {
            return Collections.emptyList();
        }

        String loginSeguro = loginAtual.trim().toLowerCase(Locale.ROOT);
        if (usarPersistenciaDuravel()) {
            try {
                List<ChatMencao> recentes = chatMencaoRepository.findByDestinatarioLoginOrderByIdDesc(
                        loginSeguro, PageRequest.of(0, limite));
                return mapearParaNotificacao(recentes);
            } catch (RuntimeException ex) {
                ativarFallbackMemoria("FALHA_PERSISTENCIA_HISTORY", ex);
            }
        }

        Deque<MentionNotification> historico = historicoMencoes.computeIfAbsent(loginSeguro, key -> new ArrayDeque<>());
        synchronized (historico) {
            if (historico.isEmpty()) {
                return Collections.emptyList();
            }
            List<MentionNotification> copia = new ArrayList<>(historico);
            int inicio = Math.max(0, copia.size() - limite);
            List<MentionNotification> recentes = new ArrayList<>(copia.subList(inicio, copia.size()));
            Collections.reverse(recentes);
            return recentes;
        }
    }

    @Override
    public boolean isColdStartAtivo() {
        return System.currentTimeMillis() - startedAt < COLD_START_WINDOW_MS;
    }

    @Override
    public boolean isModoMemoriaLocal() {
        return fallbackMemoriaAtivo || chatMencaoRepository == null;
    }

    private void registrarMencoesDuravel(String autorLoginSeguro,
                                         String autorNomeExibicao,
                                         Long mensagemId,
                                         String mensagemPreview,
                                         Set<String> destinatarios) {
        for (String loginDestino : destinatarios) {
            PersistOutcome outcome = salvarMencaoDuravelIdempotente(
                    loginDestino, autorLoginSeguro, autorNomeExibicao, mensagemId, mensagemPreview);
            if (outcome == PersistOutcome.DUPLICATE_RACE_IGNORED) {
                LOGGER.debug("[CHAT][MENTION][ENQUEUE] mode=PERSISTENT_DB from={} to={} messageId={} status=DUPLICATE_RACE_IGNORED",
                        autorLoginSeguro, loginDestino, mensagemId);
                continue;
            }

            int descartadasPendentes = trimPendenciasSeNecessario(loginDestino);
            int descartadasHistorico = trimHistoricoSeNecessario(loginDestino);
            long pendentesAposInclusao = chatMencaoRepository.countByDestinatarioLoginAndDataConfirmacaoIsNull(loginDestino);
            long historicoAposInclusao = chatMencaoRepository.countByDestinatarioLogin(loginDestino);

            LOGGER.debug("[CHAT][MENTION][ENQUEUE] mode=PERSISTENT from={} to={} messageId={} pending={} history={} droppedPending={} droppedHistory={}",
                    autorLoginSeguro, loginDestino, mensagemId, pendentesAposInclusao, historicoAposInclusao,
                    descartadasPendentes, descartadasHistorico);
        }
    }

    private void registrarMencoesMemoria(String autorLoginSeguro,
                                         String autorNomeExibicao,
                                         Long mensagemId,
                                         String mensagemPreview,
                                         Set<String> destinatarios) {
        for (String loginDestino : destinatarios) {
            MentionNotification notificacao = new MentionNotification(
                    autorLoginSeguro,
                    autorNomeExibicao,
                    mensagemId,
                    mensagemPreview);
            Deque<MentionNotification> fila = mencoesPendentes.computeIfAbsent(loginDestino, key -> new ArrayDeque<>());
            int pendentesAposInclusao;
            int descartadasPendentes = 0;
            synchronized (fila) {
                fila.addLast(notificacao);
                while (fila.size() > MAX_PENDING_NOTIFICATIONS) {
                    fila.pollFirst();
                    descartadasPendentes++;
                }
                pendentesAposInclusao = fila.size();
            }
            Deque<MentionNotification> historico = historicoMencoes.computeIfAbsent(loginDestino, key -> new ArrayDeque<>());
            int historicoAposInclusao;
            int descartadasHistorico = 0;
            synchronized (historico) {
                historico.addLast(notificacao);
                while (historico.size() > MAX_HISTORY_NOTIFICATIONS) {
                    historico.pollFirst();
                    descartadasHistorico++;
                }
                historicoAposInclusao = historico.size();
            }
            LOGGER.debug("[CHAT][MENTION][ENQUEUE] mode=MEMORY_LOCAL from={} to={} messageId={} pending={} history={} droppedPending={} droppedHistory={}",
                    autorLoginSeguro, loginDestino, mensagemId, pendentesAposInclusao, historicoAposInclusao,
                    descartadasPendentes, descartadasHistorico);
        }
    }

    private List<MentionNotification> mapearParaNotificacao(List<ChatMencao> mencoes) {
        if (mencoes == null || mencoes.isEmpty()) {
            return Collections.emptyList();
        }
        List<MentionNotification> notificacoes = new ArrayList<>(mencoes.size());
        for (ChatMencao mencao : mencoes) {
            if (mencao == null) {
                continue;
            }
            notificacoes.add(new MentionNotification(
                    mencao.getAutorLogin(),
                    mencao.getAutorNomeExibicao(),
                    mencao.getChatMensagemId(),
                    mencao.getMensagemPreview()
            ));
        }
        return notificacoes;
    }

    private int trimPendenciasSeNecessario(String loginDestino) {
        long totalPendencias = chatMencaoRepository.countByDestinatarioLoginAndDataConfirmacaoIsNull(loginDestino);
        int excedente = (int) (totalPendencias - MAX_PENDING_NOTIFICATIONS);
        if (excedente <= 0) {
            return 0;
        }
        List<ChatMencao> antigas = chatMencaoRepository.findByDestinatarioLoginAndDataConfirmacaoIsNullOrderByIdAsc(
                loginDestino, PageRequest.of(0, excedente));
        chatMencaoRepository.deleteAll(antigas);
        return antigas.size();
    }

    private int trimHistoricoSeNecessario(String loginDestino) {
        long totalHistorico = chatMencaoRepository.countByDestinatarioLogin(loginDestino);
        int excedente = (int) (totalHistorico - MAX_HISTORY_NOTIFICATIONS);
        if (excedente <= 0) {
            return 0;
        }
        List<ChatMencao> antigas = chatMencaoRepository.findByDestinatarioLoginOrderByIdAsc(
                loginDestino, PageRequest.of(0, excedente));
        chatMencaoRepository.deleteAll(antigas);
        return antigas.size();
    }

    private Set<String> normalizarDestinatarios(String autorLoginSeguro, Set<String> destinatarios) {
        Set<String> destinatariosSeguros = new HashSet<>();
        for (String destinatario : destinatarios) {
            if (ValidacaoUtils.isVazia(destinatario)) {
                continue;
            }
            String loginDestino = destinatario.trim().toLowerCase(Locale.ROOT);
            if (!loginDestino.equals(autorLoginSeguro)) {
                destinatariosSeguros.add(loginDestino);
            }
        }
        return destinatariosSeguros;
    }

    private boolean usarPersistenciaDuravel() {
        if (chatMencaoRepository == null) {
            return false;
        }
        if (!fallbackMemoriaAtivo) {
            return true;
        }
        tentarRecuperarPersistencia();
        return !fallbackMemoriaAtivo;
    }

    private synchronized void tentarRecuperarPersistencia() {
        if (!fallbackMemoriaAtivo || chatMencaoRepository == null) {
            return;
        }
        long agora = System.currentTimeMillis();
        if (agora < nextRecoveryAttemptAt) {
            return;
        }

        LOGGER.info("[CHAT][MENTION][MODE] mode=MEMORY_LOCAL_EPHEMERAL status=RECOVERY_ATTEMPT failures={} pendingUsers={}",
                recoveryFailures, mencoesPendentes.size());

        try {
            chatMencaoRepository.countByDestinatarioLoginAndDataConfirmacaoIsNull(RECOVERY_PROBE_LOGIN);
            int replayed = replayPendenciasMemoriaParaPersistencia();
            fallbackMemoriaAtivo = false;
            nextRecoveryAttemptAt = 0L;
            recoveryFailures = 0;
            long degradadoMs = fallbackStartedAt > 0 ? (agora - fallbackStartedAt) : 0L;
            fallbackStartedAt = 0L;
            LOGGER.info("[CHAT][MENTION][MODE] mode=PERSISTENT_DB status=RECOVERED replayed={} degradedMs={}",
                    replayed, degradadoMs);
        } catch (RuntimeException ex) {
            recoveryFailures++;
            long backoff = calcularBackoffRecuperacao(recoveryFailures);
            nextRecoveryAttemptAt = agora + backoff;
            LOGGER.warn("[CHAT][MENTION][MODE] mode=MEMORY_LOCAL_EPHEMERAL status=RECOVERY_FAILED failures={} nextRetryMs={} cause={}",
                    recoveryFailures, backoff, ex.getClass().getSimpleName());
        }
    }

    private int replayPendenciasMemoriaParaPersistencia() {
        int replayed = 0;
        for (String loginDestino : mencoesPendentes.keySet()) {
            Deque<MentionNotification> fila = mencoesPendentes.get(loginDestino);
            if (fila == null) {
                continue;
            }
            List<MentionNotification> snapshot;
            synchronized (fila) {
                if (fila.isEmpty()) {
                    continue;
                }
                snapshot = new ArrayList<>(fila);
            }
            for (MentionNotification notificacao : snapshot) {
                if (notificacao == null || notificacao.getChatMensagemId() == null) {
                    continue;
                }
                PersistOutcome outcome = salvarMencaoDuravelIdempotente(
                        loginDestino,
                        notificacao.getAutorLogin(),
                        notificacao.getAutorNomeExibicao(),
                        notificacao.getChatMensagemId(),
                        notificacao.getMensagemPreview());
                if (outcome != PersistOutcome.ERROR) {
                    replayed++;
                }
            }
            synchronized (fila) {
                fila.clear();
            }
        }
        return replayed;
    }

    private long calcularBackoffRecuperacao(int failures) {
        int expoente = Math.max(0, Math.min(8, failures - 1));
        long backoff = RECOVERY_BASE_BACKOFF_MS * (1L << expoente);
        return Math.min(backoff, RECOVERY_MAX_BACKOFF_MS);
    }

    private synchronized void ativarFallbackMemoria(String reason, RuntimeException ex) {
        if (fallbackMemoriaAtivo) {
            return;
        }
        fallbackMemoriaAtivo = true;
        fallbackStartedAt = System.currentTimeMillis();
        nextRecoveryAttemptAt = fallbackStartedAt + RECOVERY_BASE_BACKOFF_MS;
        recoveryFailures = 0;
        LOGGER.warn("[CHAT][MENTION][MODE] mode=MEMORY_LOCAL_EPHEMERAL status=DEGRADED reason={} cause={}",
                reason, ex.getClass().getSimpleName());
    }

    public void setChatMencaoRepository(ChatMencaoRepository chatMencaoRepository) {
        this.chatMencaoRepository = chatMencaoRepository;
        if (chatMencaoRepository != null) {
            LOGGER.info("[CHAT][MENTION][MODE] mode=PERSISTENT_DB status=READY");
        } else {
            LOGGER.warn("[CHAT][MENTION][MODE] mode=MEMORY_LOCAL_EPHEMERAL status=DEGRADED reason=REPOSITORY_NAO_INJETADO");
        }
    }

    private String buildMensagemPreview(String texto) {
        if (texto == null) {
            return "";
        }
        String valor = texto.trim();
        if (valor.length() <= 100) {
            return valor;
        }
        return valor.substring(0, 100).trim() + "...";
    }

    private PersistOutcome salvarMencaoDuravelIdempotente(String loginDestino,
                                                          String autorLoginSeguro,
                                                          String autorNomeExibicao,
                                                          Long mensagemId,
                                                          String mensagemPreview) {
        ChatMencao mencao = new ChatMencao();
        mencao.setDestinatarioLogin(loginDestino);
        mencao.setAutorLogin(autorLoginSeguro);
        mencao.setAutorNomeExibicao(normalizarTextoObrigatorio(autorNomeExibicao));
        mencao.setChatMensagemId(mensagemId);
        mencao.setMensagemPreview(normalizarTextoObrigatorio(mensagemPreview));
        mencao.setDataCriacao(new Date());
        try {
            chatMencaoRepository.save(mencao);
            return PersistOutcome.SAVED;
        } catch (RuntimeException ex) {
            if (isViolacaoUnicidadeMencao(ex)) {
                return PersistOutcome.DUPLICATE_RACE_IGNORED;
            }
            throw ex;
        }
    }

    private String normalizarTextoObrigatorio(String valor) {
        return valor == null ? "" : valor;
    }

    private boolean isViolacaoUnicidadeMencao(RuntimeException ex) {
        Throwable atual = ex;
        while (atual != null) {
            if (atual instanceof ConstraintViolationException) {
                String constraint = ((ConstraintViolationException) atual).getConstraintName();
                if (constraint != null && UNIQUE_CONSTRAINT_MENCAO.equalsIgnoreCase(constraint.trim())) {
                    return true;
                }
            }
            if (atual instanceof DataIntegrityViolationException) {
                String msg = atual.getMessage();
                if (msg != null && msg.toUpperCase(Locale.ROOT).contains(UNIQUE_CONSTRAINT_MENCAO)) {
                    return true;
                }
            }
            String msg = atual.getMessage();
            if (msg != null) {
                String normalizada = msg.toUpperCase(Locale.ROOT);
                if (normalizada.contains(UNIQUE_CONSTRAINT_MENCAO)) {
                    return true;
                }
                if ((normalizada.contains("DUPLICATE") || normalizada.contains("UNIQUE"))
                        && normalizada.contains("CHM_DEST_LOGIN")
                        && normalizada.contains("CHM_CHT_ID")) {
                    return true;
                }
            }
            atual = atual.getCause();
        }
        return false;
    }

    private enum PersistOutcome {
        SAVED,
        DUPLICATE_RACE_IGNORED,
        ERROR
    }
}
