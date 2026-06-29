package com.opendev.bolao.service.impl;

import com.opendev.bolao.service.ChatNotificationService;
import com.opendev.bolao.service.dto.MentionNotification;
import com.opendev.bolao.util.ValidacaoUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ChatNotificationServiceImpl implements ChatNotificationService {

    private static final int MAX_PENDING_NOTIFICATIONS = 50;
    private final ConcurrentMap<String, Deque<MentionNotification>> mencoesPendentes = new ConcurrentHashMap<>();

    @Override
    public void registrarMencoes(String autorLogin,
                                 String autorNomeExibicao,
                                 String texto,
                                 Long mensagemId,
                                 Set<String> destinatarios) {
        if (destinatarios == null || destinatarios.isEmpty() || ValidacaoUtils.isVazia(autorLogin)) {
            return;
        }

        String autorLoginSeguro = autorLogin.trim().toLowerCase(Locale.ROOT);
        String mensagemPreview = buildMensagemPreview(texto);

        for (String destinatario : destinatarios) {
            if (ValidacaoUtils.isVazia(destinatario)) {
                continue;
            }
            String loginDestino = destinatario.trim().toLowerCase(Locale.ROOT);
            if (loginDestino.equals(autorLoginSeguro)) {
                continue;
            }
            MentionNotification notificacao = new MentionNotification(
                    autorLoginSeguro,
                    autorNomeExibicao,
                    mensagemId,
                    mensagemPreview);
            Deque<MentionNotification> fila = mencoesPendentes.computeIfAbsent(loginDestino, key -> new ArrayDeque<>());
            synchronized (fila) {
                fila.addLast(notificacao);
                while (fila.size() > MAX_PENDING_NOTIFICATIONS) {
                    fila.pollFirst();
                }
            }
        }
    }

    @Override
    public List<MentionNotification> buscarMencoesPendentes(String loginAtual) {
        if (ValidacaoUtils.isVazia(loginAtual)) {
            return Collections.emptyList();
        }

        String loginSeguro = loginAtual.trim().toLowerCase(Locale.ROOT);
        Deque<MentionNotification> fila = mencoesPendentes.remove(loginSeguro);
        if (fila == null || fila.isEmpty()) {
            return Collections.emptyList();
        }
        synchronized (fila) {
            return new ArrayList<>(fila);
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
}
