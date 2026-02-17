package com.opendev.bolao.service.impl;

import java.sql.Time;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.opendev.bolao.dao.EquipeDao;
import com.opendev.bolao.dao.JogoDao;
import com.opendev.bolao.dao.ParticipanteDao;
import com.opendev.bolao.email.Email;
import com.opendev.bolao.model.Equipe;
import com.opendev.bolao.model.Jogo;
import com.opendev.bolao.model.Participante;
import com.opendev.bolao.service.JogoService;
import com.opendev.bolao.util.ConversaoUtils;
import com.opendev.bolao.util.FiltroBuscaJogos;

public class JogoServiceImpl implements JogoService {
    
    private static final Log logger = LogFactory.getLog(JogoService.class);
	
	private JogoDao jogoDao;
	private EquipeDao equipeDao;
    private ParticipanteDao participanteDao;

	public Long criarNovoJogo(Jogo jogo, Long equipe1Id, Long equipe2Id) {
		Equipe equipe1 = getEquipeDao().buscarPorId(equipe1Id);
		Equipe equipe2 = getEquipeDao().buscarPorId(equipe2Id);
		jogo.setEquipe1(equipe1);
		jogo.setEquipe2(equipe2);
		getJogoDao().salvar(jogo);
		return jogo.getId();
	}

	public List buscarTodos() {
		return getJogoDao().buscarTodos();
	}
	
	public void atualizarResultado(Long idJogo, Integer golsEquipe1, Integer golsEquipe2) {
		Jogo jogo = getJogoDao().buscarPorId(idJogo);
		jogo.setGolsEquipe1(golsEquipe1);
		jogo.setGolsEquipe2(golsEquipe2);
		Participante.expirarCacheDeClassificacao();
	}
	
	public List buscarUsandoFiltro(FiltroBuscaJogos filtro) {
		return getJogoDao().buscarUsandoFiltro(filtro);
	}
    
    public void avisarSobreProximoJogo() {
        Calendar calendar = Calendar.getInstance();
        logger.info(getClass() + ".avisarSobreProximoJogo() - " + calendar);
        calendar.add(Calendar.HOUR_OF_DAY, 2);
        Date data = calendar.getTime();
        Time hora = ConversaoUtils.converterParaTempo(calendar.get(Calendar.HOUR_OF_DAY));
        logger.info("Buscando jogos da data = " + data + ", hora = " + hora);
        List jogos = getJogoDao().buscarPorDataEHora(data, hora);
        logger.info("Quantidade de jogos encontrados = " + jogos.size());
        StringBuffer jogosHtml = new StringBuffer();
        if (jogos != null && !jogos.isEmpty()) {
            for (Iterator iter = jogos.iterator(); iter.hasNext();) {
                Jogo jogo = (Jogo) iter.next();
                jogosHtml.append("<tr>");
                jogosHtml.append("<td align=\"center\">");
                jogosHtml.append(jogo.getData());
                jogosHtml.append("</td>");
                jogosHtml.append("<td align=\"center\">");
                jogosHtml.append(jogo.getHora());
                jogosHtml.append("</td>");
                jogosHtml.append("<td>");
                jogosHtml.append(jogo.getLocal());
                jogosHtml.append("</td>");
                jogosHtml.append("<td align=\"center\">");
                jogosHtml.append(jogo.getRepresentacaoEquipes());
                jogosHtml.append("</td>");
                jogosHtml.append("</tr>");
            }
            Email email = new Email("proximosJogos.html", "Atenção aos próximos jogos!");
            email.setPropriedade("jogos", jogosHtml.toString());
            List participantes = getParticipanteDao().buscarTodosDoBolaoGeral();
            for (Iterator iter = participantes.iterator(); iter.hasNext();) {
                Participante participante = (Participante) iter.next();
                email.adicionarEnderecoDestino(participante.getEmail());
            }
            try {
                email.enviar();
            } catch (Exception e) {
                logger.error("Erro ao enviar email!", e);
            }
        }
    }
    
    public List buscarJogosDeHoje() {
        Date dataHoje = new Date();
        return getJogoDao().buscarPorData(dataHoje);
    }

	public JogoDao getJogoDao() {
		return jogoDao;
	}

	public void setJogoDao(JogoDao jogoDao) {
		this.jogoDao = jogoDao;
	}

	public EquipeDao getEquipeDao() {
		return equipeDao;
	}

	public void setEquipeDao(EquipeDao equipeDao) {
		this.equipeDao = equipeDao;
	}

    
    public ParticipanteDao getParticipanteDao() {
        return this.participanteDao;
    }
    
    public void setParticipanteDao(ParticipanteDao participanteDao) {
        this.participanteDao = participanteDao;
    }

}
