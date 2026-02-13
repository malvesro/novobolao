package com.opendev.bolao.dao;

import java.util.List;

import com.opendev.bolao.model.Jogo;
import com.opendev.bolao.model.Palpite;
import com.opendev.bolao.model.Participante;

public interface PalpiteDao {

	public Palpite buscarPorParticipanteEJogo(Participante participante, Jogo jogo);

	public void salvar(Palpite palpite);

	public Palpite buscarPorParticipanteEJogo(String login, Long idJogo);

    public List buscarPorJogo(Long idJogo);

	public List buscarPorParticipante(String login);

}
