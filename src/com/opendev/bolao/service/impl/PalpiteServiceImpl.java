package com.opendev.bolao.service.impl;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;

import com.opendev.bolao.repository.JogoRepository;
import com.opendev.bolao.repository.PalpiteRepository;
import com.opendev.bolao.repository.ParticipanteRepository;
import com.opendev.bolao.model.Jogo;
import com.opendev.bolao.model.Palpite;
import com.opendev.bolao.model.Participante;
import com.opendev.bolao.service.PalpiteService;

/**
 * Implementação do serviço de Palpite.
 * Refatorado para utilizar Spring Data JPA Repositories.
 */
public class PalpiteServiceImpl implements PalpiteService {
	
	private JogoRepository jogoRepository;
	private ParticipanteRepository participanteRepository;
	private PalpiteRepository palpiteRepository;

	
	public Palpite buscarPalpiteDoJogo(String login, Long idJogo) {
		return getPalpiteRepository().findByParticipanteLoginAndIdJogo(login, idJogo);
	}
	
	public void atualizarPalpite(String login, Long idJogo, Integer golsEquipe1, Integer golsEquipe2, String ip) {
		if (!podeAtualizar(idJogo)) {
			throw new IllegalStateException("O horário limite para confirmação foi ultrapassado!");
		}
		Participante participante = getParticipanteRepository().findByLogin(login)
				.orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + login));
		
		if (participante.isAdministrador()) {
			throw new IllegalStateException("Perfis administrativos nao participam dos palpites.");
		}

		// Busca palpite existente para atualização ou cria um novo se não existir
		Palpite palpite = getPalpiteRepository().findByParticipanteLoginAndIdJogo(login, idJogo);
		if (palpite == null) {
			palpite = new Palpite();
			palpite.setIdJogo(idJogo);
			palpite.setIdParticipante(participante.getId());
		}

		palpite.setGolsEquipe1(golsEquipe1);
		palpite.setGolsEquipe2(golsEquipe2);
		palpite.setIp(ip);
		palpite.setDataHoraAtualizacao(new Timestamp(System.currentTimeMillis()));
		
		getPalpiteRepository().save(palpite);
	}
    
    public List<Palpite> buscarPalpitesDoJogo(Long idJogo) {
    	List<Palpite> palpites = getPalpiteRepository().findByIdJogo(idJogo);
    	Collections.sort(palpites);
        return palpites;
    }
	
	private boolean podeAtualizar(Long idJogo) {
		return getJogoRepository().findById(idJogo)
				.map(Jogo::getPodeDarPalpite)
				.orElse(false);
	}
	
	public List<Palpite> buscarPalpitesDoParticipante(String login) {
		return getPalpiteRepository().findByParticipanteLogin(login);
	}
	
	public ParticipanteRepository getParticipanteRepository() {
		return participanteRepository;
	}

	public void setParticipanteRepository(ParticipanteRepository participanteRepository) {
		this.participanteRepository = participanteRepository;
	}

	public PalpiteRepository getPalpiteRepository() {
		return palpiteRepository;
	}

	public void setPalpiteRepository(PalpiteRepository palpiteRepository) {
		this.palpiteRepository = palpiteRepository;
	}

	public JogoRepository getJogoRepository() {
		return jogoRepository;
	}

	public void setJogoRepository(JogoRepository jogoRepository) {
		this.jogoRepository = jogoRepository;
	}

}
