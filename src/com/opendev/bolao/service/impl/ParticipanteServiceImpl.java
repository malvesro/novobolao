package com.opendev.bolao.service.impl;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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
import com.opendev.bolao.util.GraficoDesempenhoCacheControl;
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

	// Cache Global de Classificação (Estratégia de Arquiteto para Bolão de alta escala)
	private List<Participante> cacheRanking = null;
    private Map<Long, Integer> cachePosicoesRankingAnterior = null;
    private static final long GRAFICO_CACHE_TTL_MS = TimeUnit.MINUTES.toMillis(5);
    private final Map<String, GraficoComparativoCacheEntry> cacheGraficoComparativo = new HashMap<>();
    private long versaoCacheGraficoLocal = GraficoDesempenhoCacheControl.obterVersaoAtual();

	public synchronized List buscarClassificacao() {
		// Se o cache de dados individuais de pontuação estiver expirado, 
		// devemos invalidar o nosso cache global de ranking também.
		if (Participante.isCacheExpirado()) {
			this.cacheRanking = null;
		}

		if (this.cacheRanking != null) {
			return new ArrayList<>(this.cacheRanking);
		}

		LOGGER.info("[CACHE][RANKING] Reconstruindo ranking global...");
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
		
		// Ordenação oficial (Comparable implementado em Participante)
		Collections.sort(participantes);
        aplicarVariacaoPosicao(participantes);

		Participante.notificarCacheAtualizado();
		this.cacheRanking = Collections.unmodifiableList(participantes);
		
		return new ArrayList<>(this.cacheRanking);
	}

    private void aplicarVariacaoPosicao(List<Participante> participantesOrdenados) {
        Map<Long, Integer> posicoesAnteriores = this.cachePosicoesRankingAnterior;
        Map<Long, Integer> posicoesAtuais = new HashMap<>();

        for (int indice = 0; indice < participantesOrdenados.size(); indice++) {
            Participante participante = participantesOrdenados.get(indice);
            int posicaoAtual = indice + 1;
            Long participanteId = participante.getId();
            DadosClassificacao totais = participante.getPontuacaoTotal();

            if (participanteId != null) {
                posicoesAtuais.put(participanteId, posicaoAtual);
            }

            if (totais == null || participanteId == null || posicoesAnteriores == null) {
                if (totais != null) {
                    totais.setVariacaoPosicao(null);
                }
                continue;
            }

            Integer posicaoAnterior = posicoesAnteriores.get(participanteId);
            if (posicaoAnterior == null) {
                totais.setVariacaoPosicao(null);
                continue;
            }

            totais.setVariacaoPosicao(posicaoAnterior - posicaoAtual);
        }

        this.cachePosicoesRankingAnterior = posicoesAtuais;
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
	
	public synchronized GraficoComparativoDesempenho construirGraficoDesempenho(Participante participante, Long idRivail) {
        sincronizarVersaoDoCacheGrafico();
        if (participante == null || participante.getId() == null) {
            return construirGraficoDesempenhoSemCache(participante, idRivail);
        }

        String chaveCache = gerarChaveCacheGrafico(participante.getId(), idRivail, this.versaoCacheGraficoLocal);
        GraficoComparativoCacheEntry cacheEntry = this.cacheGraficoComparativo.get(chaveCache);
        if (cacheEntry != null && !cacheEntry.estaExpirado()) {
            return cacheEntry.grafico;
        }

        GraficoComparativoDesempenho grafico = construirGraficoDesempenhoSemCache(participante, idRivail);
        this.cacheGraficoComparativo.put(chaveCache, new GraficoComparativoCacheEntry(grafico));
        return grafico;
	}

    private void sincronizarVersaoDoCacheGrafico() {
        long versaoAtual = GraficoDesempenhoCacheControl.obterVersaoAtual();
        if (versaoAtual == this.versaoCacheGraficoLocal) {
            return;
        }
        this.cacheGraficoComparativo.clear();
        this.versaoCacheGraficoLocal = versaoAtual;
        LOGGER.info("[CACHE][GRAFICO] Cache invalidado por mudança de versão global para {}", versaoAtual);
    }

    private String gerarChaveCacheGrafico(Long participanteId, Long idRivail, long versao) {
        String rivalChave = idRivail == null ? "__self__" : String.valueOf(idRivail);
        return participanteId + "::" + rivalChave + "::v" + versao;
    }

    private GraficoComparativoDesempenho construirGraficoDesempenhoSemCache(Participante participante, Long idRivail) {
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

            // Otimização: buscar todos os palpites dos participantes envolvidos de uma vez
            List<Palpite> todosPalpites = getPalpiteRepository().findByParticipanteIn(participantes);
            Map<Long, Map<Long, Palpite>> mapaPalpites = new HashMap<>();
            for (Palpite p : todosPalpites) {
                mapaPalpites.computeIfAbsent(p.getParticipante().getId(), k -> new HashMap<>())
                        .put(p.getJogo().getId(), p);
            }

            List<Jogo> jogos = getJogoRepository().findJogosFinalizados();
            // Ordenar jogos por data para garantir a ordem cronológica no gráfico
            Collections.sort(jogos);

            seriesCollection = new TimeSeriesCollection();
            for (Participante umParticipante : participantes) {
                TimeSeries series = new TimeSeries(umParticipante.getNomeFormatado());
                long pontos = 0L;
                Map<Long, Palpite> palpitesDoParticipante = mapaPalpites.getOrDefault(umParticipante.getId(),
                        Collections.emptyMap());
                for (Jogo jogo : jogos) {
                    Palpite palpiteDoJogo = palpitesDoParticipante.get(jogo.getId());
                    if (palpiteDoJogo != null) {
                        pontos += palpiteDoJogo.getPontuacao().getPontuacao();
                    }
                    series.addOrUpdate(new Day(jogo.getData()), pontos);
                }
                seriesCollection.addSeries(series);
            }
        }
        return new GraficoComparativoDesempenho(seriesCollection);
    }

    private static final class GraficoComparativoCacheEntry {
        private final GraficoComparativoDesempenho grafico;
        private final long createdAt;

        private GraficoComparativoCacheEntry(GraficoComparativoDesempenho grafico) {
            this.grafico = grafico;
            this.createdAt = System.currentTimeMillis();
        }

        private boolean estaExpirado() {
            return System.currentTimeMillis() - this.createdAt > GRAFICO_CACHE_TTL_MS;
        }
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
                Email email = new Email("notificacaoCadastroAprovado.html", "📜 Pergaminho Validado: Vossa conta no Grande Bolão de Refactória está ativa!");
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
        String[] adminEmails = Email.getAdminEmails();
        if (adminEmails.length == 0) {
            LOGGER.warn("[CADASTRO] Nenhum e-mail de admin configurado em SMTP_ADMIN_EMAILS. Notificação de novo cadastro não será enviada.");
        } else {
            for (String adminEmail : adminEmails) {
                email.adicionarEnderecoDestino(adminEmail);
            }
        }
        email.setPropriedade("nome", participante.getNome());
        try {
            email.enviar();
        } catch (Exception e) {
            LOGGER.error("[CADASTRO] Erro ao enviar email de novo cadastro pendente para admins: {}", e.getMessage());
        }

        // Notificar o participante que o pedido foi recebido
        Email emailParticipante = criarEmail("pedidoRecebido.html", "Pedido de cadastro recebido");
        emailParticipante.adicionarEnderecoDestino(participante.getEmail());
        emailParticipante.setPropriedade("nome", participante.getNome());
        try {
            emailParticipante.enviar();
        } catch (Exception e) {
            LOGGER.error("[CADASTRO] Erro ao enviar email de pedido recebido para participante={}: {}", participante.getLogin(), e.getMessage());
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
