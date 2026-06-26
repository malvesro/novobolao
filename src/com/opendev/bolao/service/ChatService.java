package com.opendev.bolao.service;

import com.opendev.bolao.service.dto.ChatMensagemView;

import java.util.List;

public interface ChatService {

    List<ChatMensagemView> buscarMensagensIniciais(String loginAtual);

    List<ChatMensagemView> buscarMensagensIncrementais(String loginAtual, Long ultimoIdRecebido);

    List<String> buscarParticipantesOnline();

    ChatMensagemView criarMensagem(String loginAtual, String chaveSessao, String apelido, String texto, String ipOrigem);

    void atualizarPresenca(String loginAtual);
}
