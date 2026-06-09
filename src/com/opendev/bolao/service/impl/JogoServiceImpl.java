package com.opendev.bolao.service.impl;

import java.sql.Time;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.opendev.bolao.repository.EquipeRepository;
import com.opendev.bolao.repository.JogoRepository;
import com.opendev.bolao.repository.ParticipanteRepository;
import com.opendev.bolao.email.Email;
import com.opendev.bolao.model.Equipe;
import com.opendev.bolao.model.Jogo;
import com.opendev.bolao.model.Participante;
import com.opendev.bolao.service.JogoService;
import com.opendev.bolao.util.ConversaoUtils;
import com.opendev.bolao.util.FiltroBuscaJogos;

/**
 * Implementação do serviço de Jogo.
 * Refatorado para utilizar Spring Data JPA Repositories.
 */
public class JogoServiceImpl implements JogoService {
    
    private static final Log logger = LogFactory.getLog(JogoService.class);
	
	private JogoRepository jogoRepository;
	private EquipeRepository equipeRepository;
	   private ParticipanteRepository participanteRepository;

	   @PersistenceContext
	   private EntityManager entityManager;

	public Long criarNovoJogo(Jogo jogo, Long equipe1Id, Long equipe2Id) {
		Equipe equipe1 = getEquipeRepository().findById(equipe1Id)
				.orElseThrow(() -> new IllegalArgumentException("Equipe 1 não encontrada: " + equipe1Id));
		Equipe equipe2 = getEquipeRepository().findById(equipe2Id)
				.orElseThrow(() -> new IllegalArgumentException("Equipe 2 não encontrada: " + equipe2Id));
		jogo.setEquipe1(equipe1);
		jogo.setEquipe2(equipe2);
		getJogoRepository().save(jogo);
		return jogo.getId();
	}

	public List<Jogo> buscarTodos() {
		return getJogoRepository().findAll(Sort.by("data", "hora"));
	}
	
	public void atualizarResultado(Long idJogo, Integer golsEquipe1, Integer golsEquipe2) {
		getJogoRepository().findById(idJogo).ifPresent(jogo -> {
			jogo.setGolsEquipe1(golsEquipe1);
			jogo.setGolsEquipe2(golsEquipe2);
            getJogoRepository().save(jogo);
			Participante.expirarCacheDeClassificacao();
		});
	}

	public void atualizarDadosEstruturaisJogo(Long idJogo, Date data, Time hora, String local, int fase, Long idEquipe1, Long idEquipe2) {
		getJogoRepository().findById(idJogo).ifPresent(jogo -> {
			Equipe equipe1 = getEquipeRepository().findById(idEquipe1)
					.orElseThrow(() -> new IllegalArgumentException("Equipe 1 não encontrada: " + idEquipe1));
			Equipe equipe2 = getEquipeRepository().findById(idEquipe2)
					.orElseThrow(() -> new IllegalArgumentException("Equipe 2 não encontrada: " + idEquipe2));
			
			jogo.setData(data);
			jogo.setHora(hora);
			jogo.setLocal(local);
			jogo.setFase(fase);
			jogo.setEquipe1(equipe1);
			jogo.setEquipe2(equipe2);
			
			getJogoRepository().save(jogo);
			Participante.expirarCacheDeClassificacao();
		});
	}

    public Optional<Jogo> buscarPorId(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return getJogoRepository().findById(id);
    }
 
 public List buscarUsandoFiltro(FiltroBuscaJogos filtro) {
        if (filtro == null) {
            return getJogoRepository().findAll(Sort.by("data", "hora"));
        }
        Query query = entityManager.createQuery(filtro.getHqlQuery());
        filtro.popularParametrosDaHql(query);
        return query.getResultList();
 }

    public long contarJogosUsandoFiltro(FiltroBuscaJogos filtro) {
        if (filtro == null) {
            return getJogoRepository().count();
        }
        String countQuery = "select count(j) " + filtro.getHqlQuery();
        Query query = entityManager.createQuery(countQuery);
        filtro.popularParametrosDaHql(query);
        Object result = query.getSingleResult();
        if (result instanceof Number number) {
            return number.longValue();
        }
        return 0L;
    }
    
    public void avisarSobreProximoJogo() {
        Calendar calendar = Calendar.getInstance();
        logger.info(getClass() + ".avisarSobreProximoJogo() - " + calendar);
        calendar.add(Calendar.HOUR_OF_DAY, 2);
        Date data = calendar.getTime();
        Date hora = ConversaoUtils.converterParaTempo(calendar.get(Calendar.HOUR_OF_DAY));
        logger.info("Buscando jogos da data = " + data + ", hora = " + hora);
        List<Jogo> jogos = getJogoRepository().findByDataAndHora(data, hora);
        logger.info("Quantidade de jogos encontrados = " + jogos.size());
        StringBuilder jogosHtml = new StringBuilder();
        if (jogos != null && !jogos.isEmpty()) {
            for (Jogo jogo : jogos) {
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
            Email email = new Email("proximosJogos.html", "Atenção às próximas batalhas: o momento de selar seus palpites está se aproximando!");
            email.setPropriedade("jogos", jogosHtml.toString());
            List<Participante> participantes = getParticipanteRepository().findAll();
            for (Participante participante : participantes) {
                email.adicionarEnderecoDestino(participante.getEmail());
            }
            try {
                email.enviar();
            } catch (Exception e) {
                logger.error("Erro ao enviar email!", e);
            }
        }
    }
    
    public List<Jogo> buscarJogosDeHoje() {
        Date dataHoje = new Date();
        return getJogoRepository().findByData(dataHoje);
    }

	public JogoRepository getJogoRepository() {
		return jogoRepository;
	}

	public void setJogoRepository(JogoRepository jogoRepository) {
		this.jogoRepository = jogoRepository;
	}

	public EquipeRepository getEquipeRepository() {
		return equipeRepository;
	}

	public void setEquipeRepository(EquipeRepository equipeRepository) {
		this.equipeRepository = equipeRepository;
	}

    public ParticipanteRepository getParticipanteRepository() {
        return this.participanteRepository;
    }
    
    public void setParticipanteRepository(ParticipanteRepository participanteRepository) {
        this.participanteRepository = participanteRepository;
    }

}
