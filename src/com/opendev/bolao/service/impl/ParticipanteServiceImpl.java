package com.opendev.bolao.service.impl;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import com.opendev.bolao.repository.JogoRepository;
import com.opendev.bolao.repository.PalpiteRepository;
import com.opendev.bolao.repository.ParticipanteRepository;
import com.opendev.bolao.repository.PrivilegioRepository;
import com.opendev.bolao.email.Email;
import com.opendev.bolao.exception.ValidacaoException;
import com.opendev.bolao.grafico.GraficoBarraLideres;
import com.opendev.bolao.grafico.GraficoComparativoDesempenho;
import com.opendev.bolao.model.Jogo;
import com.opendev.bolao.model.Palpite;
import com.opendev.bolao.model.Participante;
import com.opendev.bolao.model.Privilegio;
import com.opendev.bolao.model.PrivilegioId;
import com.opendev.bolao.service.ParticipanteService;
import com.opendev.bolao.util.DadosClassificacao;
import com.opendev.bolao.util.SanitizationUtils;
import com.opendev.bolao.util.ValidacaoUtils;
import com.opendev.bolao.util.MensagemErro;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;


/**
 * Implementação do serviço de Participante.
 * Refatorado para utilizar Spring Data JPA Repositories.
 */
public class ParticipanteServiceImpl implements ParticipanteService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParticipanteServiceImpl.class);
	
	private ParticipanteRepository participanteRepository;
	private JogoRepository jogoRepository;
	private PrivilegioRepository privilegioRepository;
	private PalpiteRepository palpiteRepository;
	private PasswordEncoder passwordEncoder;

	public synchronized List buscarClassificacao() {
		List<Participante> participantesAll = getParticipanteRepository().findAll();
		List<Participante> participantes = new ArrayList<>();
		
		for (Participante part : participantesAll) {
			if (!part.isAdministrador()) {
				participantes.add(part);
			}
		}

		long totalDeJogos = getJogoRepository().countJogosFinalizados();
		int qtdeDeJogos = Math.toIntExact(totalDeJogos);
		for (Participante participante : participantes) {
			DadosClassificacao totais = participante.getPontuacaoTotal();
			totais.setTotalDeJogos(qtdeDeJogos);
		}
		Participante.notificarCacheAtualizado();
		return participantes;
	}

	public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

    @Override
    public void alterarSenha(String login, String senhaAtual, String novaSenha) throws ValidacaoException {
        if (login == null || senhaAtual == null || novaSenha == null) {
            throw new ValidacaoException(Collections.singletonList(new MensagemErro("Geral", "Dados insuficientes para troca de senha.", MensagemErro.SEVERIDADE_ERRO)));
        }

        Participante participante = buscarPorLogin(login)
                .orElseThrow(() -> {
                    List<MensagemErro> erros = new ArrayList<>();
                    erros.add(new MensagemErro("Login", "Participante não encontrado.", MensagemErro.SEVERIDADE_ERRO));
                    return new ValidacaoException(erros);
                });

        if (!passwordEncoder.matches(senhaAtual, participante.getSenha())) {
            throw new ValidacaoException(Collections.singletonList(new MensagemErro("Senha atual", "Senha atual incorreta.", MensagemErro.SEVERIDADE_ERRO)));
        }

        if (!ValidacaoUtils.isSenhaValida(novaSenha)) {
            throw new ValidacaoException(Collections.singletonList(new MensagemErro("Nova senha", "A nova senha deve ter entre 8 e 64 caracteres.", MensagemErro.SEVERIDADE_ERRO)));
        }

        participante.setSenha(passwordEncoder.encode(novaSenha));
        participante.setDataHoraUltimaTrocaSenha(new Timestamp(System.currentTimeMillis()));
        getParticipanteRepository().save(participante);

        LOGGER.info("[PERFIL][SENHA] Senha alterada com sucesso para usuario={}", login);
    }
	
	public GraficoComparativoDesempenho construirGraficoDesempenho(Participante participante, Long idRivail) {
		TimeSeriesCollection seriesCollection = null;
		List<Participante> participantes = null;
		if (participante != null) {
			if (idRivail != null) {
				participantes = new ArrayList<>(2);
				participantes.add(participante);
                getParticipanteRepository().findById(idRivail).ifPresent(participantes::add);
			} else {
				participantes = new ArrayList<>(1);
				participantes.add(participante);
			}
			TimeSeries series = null;
			List<Jogo> jogos = getJogoRepository().findJogosFinalizados();
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
					palpiteDoJogo = getPalpiteRepository().findByParticipanteAndJogo(umParticipante, jogo);
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

    public Optional<Participante> buscarPorLogin(String login) {
        return getParticipanteRepository().findByLogin(login);
    }

    public Optional<Participante> buscarPorEmail(String email) {
        return getParticipanteRepository().findByEmail(email);
    }

    public List buscarTodos() {
        return getParticipanteRepository().findAll();
    }

    public void atualizarAutorizacao(Long id, boolean autorizado) {
        getParticipanteRepository().findById(id).ifPresent(participante -> {
            participante.setHabilitado(autorizado);
            getParticipanteRepository().save(participante);
            Set privilegios = participante.getPrivilegios();
            if (autorizado == true && (privilegios != null && !privilegios.isEmpty())) {
                Email email = new Email("notificacaoCadastroAprovado.html", "Confirmação de cadastro");
                email.setPropriedade("nome", participante.getNome());
                email.adicionarEnderecoDestino(participante.getEmail());
                try {
                    email.enviar();
                } catch (Exception e) {
                    LOGGER.error("[CADASTRO] Erro ao enviar email de aprovacao para o participante id={}", id, e);
                }
            }
        });
    }

    /**
     * Atualiza os papéis (privilégios) de um participante.
     * @param id ID do participante.
     * @param papel Papel a ser atribuído.
     */
    public void atualizarPapel(Long id, String papel) {
        getParticipanteRepository().findById(id).ifPresent(participante -> {
            Set<Privilegio> privilegios = participante.getPrivilegios();
            if (privilegios == null) {
                privilegios = new HashSet<>();
            } else {
                for (Iterator<Privilegio> iter = privilegios.iterator(); iter.hasNext();) {
                    Privilegio p = iter.next();
                    iter.remove();
                    getPrivilegioRepository().delete(p);
                }
            }
            String papelNormalizado = normalizarPapel(papel);
            if (papelNormalizado == null) {
                participante.setPrivilegios(privilegios);
                getParticipanteRepository().save(participante);
                return;
            }
            Privilegio privilegio = new Privilegio();
            privilegio.setIdParticipante(participante.getId());
            privilegio.setPapel(papelNormalizado);
            privilegios.add(privilegio);
            participante.setPrivilegios(privilegios);
            getParticipanteRepository().save(participante);
        });
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

        List<MensagemErro> erros = new ArrayList<>();
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
    
    /**
     * Cria um novo participante no sistema.
     * @param participante Participante a ser criado.
     * @return O participante criado.
     * @throws ValidacaoException Caso haja erro de validação.
     */
    public Participante criarNovo(Participante participante) throws ValidacaoException {
        aplicarSanitizacaoCadastro(participante);
        participante.validar();
        participante.setSenha(this.passwordEncoder.encode(participante.getSenha()));
        participante.setDataHoraCadastro(new Timestamp(System.currentTimeMillis()));
        participante.setLogin(participante.getLogin() == null ? null : participante.getLogin().trim().toLowerCase());
        participante.setEmail(participante.getEmail() == null ? null : participante.getEmail().trim());
        getParticipanteRepository().save(participante);
        Email email = criarEmail("novoCadastro.html", "Novo pedido de cadastro pendente");
        email.adicionarEnderecoDestino("deinf.rochett@bc");
        email.adicionarEnderecoDestino("rosner.suporte.deinf@bcb.gov.br");
        email.setPropriedade("nome", participante.getNome());
        try {
            email.enviar();
        } catch (Exception e) {
            LOGGER.error("[CADASTRO] Erro ao enviar email de novo cadastro pendente para participante={}", participante.getLogin(), e);
        }
        return participante;
    }

    protected Email criarEmail(String template, String assunto) {
        return new Email(template, assunto);
    }
    
    public GraficoBarraLideres construirGraficoDeBarrasDosLideres() {
        DefaultCategoryDataset dataSet = new DefaultCategoryDataset();
        List<Participante> participantes = buscarClassificacao();
        Collections.sort(participantes);
        Participante participante = null;
        long pontuacaoAnterior = -1L;
        long pontuacao = -1L;
        int posicoesDiferentes = 0;
        for (int i = 0; i < participantes.size(); i++) {
            participante = participantes.get(i);
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
    
    public ParticipanteRepository getParticipanteRepository() {
        return participanteRepository;
    }

    public void setParticipanteRepository(ParticipanteRepository participanteRepository) {
        this.participanteRepository = participanteRepository;
    }

    public JogoRepository getJogoRepository() {
        return jogoRepository;
    }

    public void setJogoRepository(JogoRepository jogoRepository) {
        this.jogoRepository = jogoRepository;
    }

    /**
     * Apaga um participante pelo seu ID.
     * @param id ID do participante.
     */
    public void apagar(Long id) {
        getParticipanteRepository().deleteById(id);
    }

    public PrivilegioRepository getPrivilegioRepository() {
        return privilegioRepository;
    }

    public void setPrivilegioRepository(PrivilegioRepository privilegioRepository) {
        this.privilegioRepository = privilegioRepository;
    }

    public PalpiteRepository getPalpiteRepository() {
        return palpiteRepository;
    }

    public void setPalpiteRepository(PalpiteRepository palpiteRepository) {
        this.palpiteRepository = palpiteRepository;
    }
}
