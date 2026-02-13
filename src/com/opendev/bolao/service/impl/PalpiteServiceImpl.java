package com.opendev.bolao.service.impl;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;

import com.opendev.bolao.dao.JogoDao;
import com.opendev.bolao.dao.PalpiteDao;
import com.opendev.bolao.dao.ParticipanteDao;
import com.opendev.bolao.model.Jogo;
import com.opendev.bolao.model.Palpite;
import com.opendev.bolao.model.Participante;
import com.opendev.bolao.service.PalpiteService;

public class PalpiteServiceImpl implements PalpiteService {
	
	private JogoDao jogoDao;
	private ParticipanteDao participanteDao;
	private PalpiteDao palpiteDao;

	
	public Palpite buscarPalpiteDoJogo(String login, Long idJogo) {
		return getPalpiteDao().buscarPorParticipanteEJogo(login, idJogo);
	}
	
	public void atualizarPalpite(String login, Long idJogo, Integer golsEquipe1, Integer golsEquipe2, String ip) {
		Participante participante = getParticipanteDao().buscarPorLogin(login);
		Palpite palpite = new Palpite();
		palpite.setIdJogo(idJogo);
		palpite.setIdParticipante(participante.getId());
		palpite.setGolsEquipe1(golsEquipe1);
		palpite.setGolsEquipe2(golsEquipe2);
		palpite.setIp(ip);
		palpite.setDataHoraAtualizacao(new Timestamp(System.currentTimeMillis()));
		getPalpiteDao().salvar(palpite);
		if (!podeAtualizar(idJogo)) {
			throw new IllegalStateException("O horário limite para confirmação foi ultrapassado!");
		}
//        Email email = new Email("auditoriaPalpiteAlterado.html", "[Importante] Confirmação de palpite");
//        Equipe equipe1 = palpite.getJogo().getEquipe1();
//        Equipe equipe2 = palpite.getJogo().getEquipe2();
//        String representacaoPalpite = equipe1.getNomePais() + " " + palpite.getGolsEquipe1() + " X " + palpite.getGolsEquipe2() + " " + equipe2.getNomePais();
//        email.setPropriedade("palpite", representacaoPalpite);
//        email.setPropriedade("ip", palpite.getIp());
//        DateFormat df = new SimpleDateFormat("dd/MM/yyyy - HH:mm:SS");
//        email.setPropriedade("dh", df.format(palpite.getDataHoraAtualizacao()));
//        email.adicionarEnderecoDestino(participante.getEmail());
//        try {
//            email.enviar();
//        } catch (Exception e) {
//            // TODO logar erro
//            e.printStackTrace();
//        }
	}
    
    public List buscarPalpitesDoJogo(Long idJogo) {
    	List palpites = getPalpiteDao().buscarPorJogo(idJogo);
    	Collections.sort(palpites);
        return palpites;
    }
	
	private boolean podeAtualizar(Long idJogo) {
		Jogo jogo = getJogoDao().buscarPorId(idJogo);
		return jogo.getPodeDarPalpite();
	}
	
	public List buscarPalpitesDoParticipante(String login) {
		return getPalpiteDao().buscarPorParticipante(login);
	}
	
	public ParticipanteDao getParticipanteDao() {
		return participanteDao;
	}

	public void setParticipanteDao(ParticipanteDao participanteDao) {
		this.participanteDao = participanteDao;
	}

	public PalpiteDao getPalpiteDao() {
		return palpiteDao;
	}

	public void setPalpiteDao(PalpiteDao palpiteDao) {
		this.palpiteDao = palpiteDao;
	}

	public JogoDao getJogoDao() {
		return jogoDao;
	}

	public void setJogoDao(JogoDao jogoDao) {
		this.jogoDao = jogoDao;
	}

}
