package com.opendev.bolao.service.impl;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import com.opendev.bolao.dao.JogoDao;
import com.opendev.bolao.dao.PalpiteDao;
import com.opendev.bolao.dao.ParticipanteDao;
import com.opendev.bolao.dao.PriviledioDao;
import com.opendev.bolao.email.Email;
import com.opendev.bolao.exception.ValidacaoException;
import com.opendev.bolao.grafico.GraficoBarraLideres;
import com.opendev.bolao.grafico.GraficoComparativoDesempenho;
import com.opendev.bolao.model.Jogo;
import com.opendev.bolao.model.Palpite;
import com.opendev.bolao.model.Participante;
import com.opendev.bolao.model.Privilegio;
import com.opendev.bolao.service.ParticipanteService;
import com.opendev.bolao.util.DadosClassificacao;
import com.opendev.bolao.util.SanitizationUtils;
import com.opendev.bolao.util.MensagemErro;
import org.springframework.security.crypto.password.PasswordEncoder;


public class ParticipanteServiceImpl implements ParticipanteService {
	
	private ParticipanteDao participanteDao;
	private JogoDao jogoDao;
	private PriviledioDao privilegioDao;
	private PalpiteDao palpiteDao;
	private PasswordEncoder passwordEncoder;

	public synchronized List buscarClassificacao() {
		List participantesAll = new ArrayList(getParticipanteDao().buscarTodosDoBolaoGeral());
		List participantes = new ArrayList();
		
		for (Object p : participantesAll) {
			Participante part = (Participante) p;
			if (!part.isAdministrador()) {
				participantes.add(part);
			}
		}

		Participante participante = null;
			long totalDeJogos = getJogoDao().buscarQuantidadeDeJogosOcorridos();
			int qtdeDeJogos = Math.toIntExact(totalDeJogos);
		DadosClassificacao totais = null;
		for (Iterator iter = participantes.iterator(); iter.hasNext();) {
			participante = (Participante) iter.next();
			totais = participante.getPontuacaoTotal();
			totais.setTotalDeJogos(qtdeDeJogos);
		}
		Participante.notificarCacheAtualizado();
		return participantes;
	}
    // ... other methods ...
	public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}
	
	public GraficoComparativoDesempenho construirGraficoDesempenho(Participante participante, Long idRivail) {
		TimeSeriesCollection seriesCollection = null;
		List participantes = null;
		if (participante != null) {
			if (idRivail != null) {
				participantes = new ArrayList(2);
				participantes.add(participante);
                participantes.add(getParticipanteDao().buscarPorId(idRivail));
			} else {
				participantes = new ArrayList(1);
				participantes.add(participante);
			}
			TimeSeries series = null;
			List jogos = getJogoDao().buscarJogosOcorridos();
			Palpite palpiteDoJogo = null;
			Participante umParticipante = null;
			Jogo jogo = null;
			long pontos = 0L;
			seriesCollection = new TimeSeriesCollection();
			for (Iterator iter = participantes.iterator(); iter.hasNext();) {
				umParticipante = (Participante) iter.next();
				series = new TimeSeries(umParticipante.getNomeFormatado());
				for (int i = 0; i < jogos.size(); i++) {
					jogo = (Jogo) jogos.get(i);
					palpiteDoJogo = getPalpiteDao().buscarPorParticipanteEJogo(umParticipante, jogo);
					if (palpiteDoJogo != null) {
						pontos += palpiteDoJogo.getPontuacao().getPontuacao();
					}
					series.addOrUpdate(new Day(jogo.getData()), pontos);
				}
                pontos = 0L;
				seriesCollection.addSeries(series);
			}
		}
        return new GraficoComparativoDesempenho(seriesCollection);
	}

    public Participante buscarPorLogin(String login) {
        return getParticipanteDao().buscarPorLogin(login);
    }

    public Participante buscarPorEmail(String email) {
        return getParticipanteDao().buscarPorEmail(email);
    }

    public List buscarTodos() {
        return getParticipanteDao().buscarTodos();
    }

    public void atualizarAutorizacao(Long id, boolean autorizado) {
        Participante participante = getParticipanteDao().buscarPorId(id);
        participante.setHabilitado(autorizado);
        Set privilegios = participante.getPrivilegios();
        if (autorizado == true && (privilegios != null && !privilegios.isEmpty())) {
            Email email = new Email("notificacaoCadastroAprovado.html", "Confirmaï¿½ï¿½o de cadastro");
            email.setPropriedade("nome", participante.getNome());
            email.adicionarEnderecoDestino(participante.getEmail());
            try {
                email.enviar();
            } catch (Exception e) {
                e.printStackTrace();
                // TODO logar erro ao enviar;
            }
        }
    }

    public void atualizarPapel(Long id, String papel) {
        Participante participante = getParticipanteDao().buscarPorId(id);
        Set privilegios = participante.getPrivilegios();
        if (privilegios == null) {
            privilegios = new HashSet();
        } else {
            for (Iterator iter = privilegios.iterator(); iter.hasNext();) {
				Privilegio p = (Privilegio) iter.next();
				iter.remove();
				getPrivilegioDao().apagar(p);
			}
        }
        String papelNormalizado = normalizarPapel(papel);
        participante.setPrivilegios(privilegios);
        if (papelNormalizado == null) {
            return;
        }
        Privilegio privilegio = new Privilegio();
        privilegio.setIdParticipante(participante.getId());
        privilegio.setPapel(papelNormalizado);
        privilegios.add(privilegio);
    }

    private String normalizarPapel(String papel) {
        if (papel == null) {
            return null;
        }
        String valor = papel.trim();
        if (valor.isEmpty() || "Nenhum".equalsIgnoreCase(valor)) {
            return null;
        }
        if (valor.startsWith("ROLE_")) {
            return valor.toUpperCase(Locale.ROOT);
        }
        return "ROLE_" + valor.toUpperCase(Locale.ROOT);
    }

    private void aplicarSanitizacaoCadastro(Participante participante) throws ValidacaoException {
        if (participante == null) {
            return;
        }

        List erros = new ArrayList();
        String loginOriginal = participante.getLogin();
        String nomeOriginal = participante.getNome();
        String emailOriginal = participante.getEmail();

        boolean loginComHtml = participante.isLoginPossuiMarkup() || SanitizationUtils.containsHtml(loginOriginal);
        boolean nomeComHtml = participante.isNomePossuiMarkup() || SanitizationUtils.containsHtml(nomeOriginal);
        boolean emailComHtml = participante.isEmailPossuiMarkup() || SanitizationUtils.containsHtml(emailOriginal);

        if (loginComHtml) {
            erros.add(new MensagemErro("Login", "Conteudo invalido (HTML nao permitido).", MensagemErro.SEVERIDADE_ERRO));
        }
        if (nomeComHtml) {
            erros.add(new MensagemErro("Nome", "Conteudo invalido (HTML nao permitido).", MensagemErro.SEVERIDADE_ERRO));
        }
        if (emailComHtml) {
            erros.add(new MensagemErro("E-mail", "Conteudo invalido (HTML nao permitido).", MensagemErro.SEVERIDADE_ERRO));
        }

        participante.setLogin(SanitizationUtils.cleanText(loginOriginal, 32));
        participante.setNome(SanitizationUtils.cleanText(nomeOriginal, 80));
        participante.setEmail(SanitizationUtils.cleanText(emailOriginal, 254));

        if (!erros.isEmpty()) {
            throw new ValidacaoException(erros);
        }
    }
    
    public Participante criarNovo(Participante participante) throws ValidacaoException {
        aplicarSanitizacaoCadastro(participante);
        participante.validar();
        participante.setSenha(this.passwordEncoder.encode(participante.getSenha()));
        participante.setDataHoraCadastro(new Timestamp(System.currentTimeMillis()));
        participante.setLogin(participante.getLogin() == null ? null : participante.getLogin().trim().toLowerCase());
        participante.setEmail(participante.getEmail() == null ? null : participante.getEmail().trim());
        getParticipanteDao().salvar(participante);
        Email email = criarEmail("novoCadastro.html", "Novo pedido de cadastro pendente");
        email.adicionarEnderecoDestino("deinf.rochett@bc");
        email.adicionarEnderecoDestino("rosner.suporte.deinf@bcb.gov.br");
        email.setPropriedade("nome", participante.getNome());
        try {
            email.enviar();
        } catch (Exception e) {
            // TODO logar esception
            e.printStackTrace();
        }
        return participante;
    }

    protected Email criarEmail(String template, String assunto) {
        return new Email(template, assunto);
    }
    
    public GraficoBarraLideres construirGraficoDeBarrasDosLideres() {
        DefaultCategoryDataset dataSet = new DefaultCategoryDataset();
        List participantes = buscarClassificacao();
        Collections.sort(participantes);
        Participante participante = null;
        long pontuacaoAnterior = -1L;
        long pontuacao = -1L;
        int posicoesDiferentes = 0;
        for (int i = 0; i < participantes.size(); i++) {
            participante = (Participante) participantes.get(i);
            pontuacao = participante.getPontuacaoTotal().getPontuacao();
            if (pontuacaoAnterior != pontuacao) {
                posicoesDiferentes++;
            }
            if (i >= 5 || posicoesDiferentes >= 4) {
                break;
            }
            dataSet.addValue(BigInteger.valueOf(pontuacao), participante.getNomeFormatado(), BigInteger.valueOf(pontuacao));
            pontuacaoAnterior = pontuacao;
        }
        return new GraficoBarraLideres(dataSet);
    }
    
    public ParticipanteDao getParticipanteDao() {
        return participanteDao;
    }

    public void setParticipanteDao(ParticipanteDao participanteDao) {
        this.participanteDao = participanteDao;
    }

	public JogoDao getJogoDao() {
		return jogoDao;
	}

	public void setJogoDao(JogoDao jogoDao) {
		this.jogoDao = jogoDao;
	}

    public void apagar(Long id) {
        getParticipanteDao().apagar(id);
    }

	public PriviledioDao getPrivilegioDao() {
		return privilegioDao;
	}

	public void setPrivilegioDao(PriviledioDao privilegioDao) {
		this.privilegioDao = privilegioDao;
	}

	public PalpiteDao getPalpiteDao() {
		return palpiteDao;
	}

	public void setPalpiteDao(PalpiteDao palpiteDao) {
		this.palpiteDao = palpiteDao;
	}



	
}
