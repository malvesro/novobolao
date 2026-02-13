package com.opendev.bolao.service;

import java.util.List;

import com.opendev.bolao.model.Palpite;

public interface PalpiteService {
	
	public void atualizarPalpite(String login, Long idPalpite, Integer golsEquipe1, Integer golsEquipe2, String ip);

	public Palpite buscarPalpiteDoJogo(String login, Long idJogo);

    public List buscarPalpitesDoJogo(Long idJogo);

	public List buscarPalpitesDoParticipante(String login);

}
