package com.opendev.bolao.service;

import com.opendev.bolao.service.dto.MentionNotification;

import java.util.List;
import java.util.Set;

public interface ChatNotificationService {

    void registrarMencoes(String autorLogin,
                          String autorNomeExibicao,
                          String texto,
                          Long mensagemId,
                          Set<String> destinatarios);

    List<MentionNotification> buscarMencoesPendentes(String loginAtual);

    int confirmarMencoesPendentes(String loginAtual, Set<Long> mensagemIds);

    int contarMencoesPendentes(String loginAtual);

    List<MentionNotification> buscarHistoricoMencoes(String loginAtual, int limite);

    boolean isColdStartAtivo();

    boolean isModoMemoriaLocal();
}
