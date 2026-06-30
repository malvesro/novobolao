package com.opendev.bolao.service;

import com.opendev.bolao.service.dto.ChatMensagemView;
import com.opendev.bolao.service.dto.MentionNotification;

import java.util.Date;
import java.util.List;

public interface ChatService {

    List<ChatMensagemView> buscarMensagensIniciais(String loginAtual);

    List<ChatMensagemView> buscarMensagensIncrementais(String loginAtual, Long ultimoIdRecebido);

    List<String> buscarParticipantesOnline();

    ChatMensagemView criarMensagem(String loginAtual, String chaveSessao, String texto, String ipOrigem);

    ChatMensagemView criarMensagem(String loginAtual, String chaveSessao, String texto, String ipOrigem, Long replyToMensagemId);

    void atualizarPresenca(String loginAtual);

    List<MentionNotification> buscarMencoesPendentes(String loginAtual);

    int confirmarMencoesPendentes(String loginAtual, List<Long> mensagemIds);

    int contarMencoesPendentes(String loginAtual);

    List<MentionNotification> buscarHistoricoMencoes(String loginAtual, int limite);

    List<ChatMensagemView> buscarHistoricoFiltrado(String loginAtual,
                                                   String termo,
                                                   String autorLogin,
                                                   Date dataInicio,
                                                   Date dataFim,
                                                   Long cursorId,
                                                   int limite);

    boolean isMencoesColdStartAtivo();

    boolean isMencoesModoDegradado();
}
