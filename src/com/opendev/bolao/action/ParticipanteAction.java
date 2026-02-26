package com.opendev.bolao.action;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpSession;

import com.opendev.bolao.exception.ValidacaoException;
import com.opendev.bolao.grafico.GraficoBarraLideres;
import com.opendev.bolao.grafico.GraficoComparativoDesempenho;
import com.opendev.bolao.model.Palpite;
import com.opendev.bolao.model.Participante;
import com.opendev.bolao.service.EquipeService;
import com.opendev.bolao.service.JogoService;
import com.opendev.bolao.service.PalpiteService;
import com.opendev.bolao.service.ParticipanteService;
import com.opendev.bolao.util.ConversaoUtils;
import com.opendev.bolao.util.MensagemErro;
import com.opendev.bolao.util.FiltroBuscaJogos;
import com.opendev.bolao.util.RequestUtils;
import com.opendev.bolao.util.SanitizationUtils;
import com.opendev.bolao.util.ValidacaoUtils;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.apache.struts2.ActionSupport;
import org.apache.struts2.interceptor.parameter.StrutsParameter;

public class ParticipanteAction extends ActionSupport {

	private static final long serialVersionUID = 1L;
	
	private PalpiteService palpiteService;
    private ParticipanteService participanteService;
    private JogoService jogoService;
    private EquipeService equipeService;
    private boolean telaPalpites;
    private List jogos;
    private List palpites;
    private List errosInclusao;
    private Participante tentativaInclusao;
    private boolean sucessoCadastro;
    private List participantes;
    private List equipes;
    private FiltroBuscaJogos filtro;
    private Participante participanteLogado;
    private Map<Long, Palpite> palpitesUsuario;
    private Long jogoId;
    private Integer palpiteGolsEquipe1;
    private Integer palpiteGolsEquipe2;
    private boolean palpiteAtualizado;
    private String palpiteErro;
    
    // Dados página principal
    private List jogosDeHoje;
    private InputStream graficoStream;
    private String rival;

    // Parâmetros de Cadastro
    private String login;
    private String nome;
    private String email;
    private String senha;

    // Parâmetros de Filtro
    private boolean usarFiltro;
    private String dataInicial;
    private String dataFinal;
    private Integer filtroFase;
    private Long filtroEquipe;
    private String filtroGrupo;
    private boolean filtroSemPalpite;
    private boolean filtroJogosNaoOcorreram;
    private boolean loginPossuiHtml;
    private boolean nomePossuiHtml;
    private boolean emailPossuiHtml;

    public String index() {
        return SUCCESS;
    }

    public String login() {
        return SUCCESS;
    }

    public String cadastroForm() {
        return SUCCESS;
    }

    public String regras() {
        return SUCCESS;
    }

    public String batePapo() {
        return SUCCESS;
    }

    public String trocaSenha() {
        return SUCCESS;
    }

	public String logout() {
		HttpSession session = RequestUtils.getRequest().getSession();
		session.invalidate();
		return SUCCESS;
	}
	
public String prepararInfoPalpites() {
        FiltroBuscaJogos filtro = obterFiltro();
        if (filtro == null) {
            setJogos(getJogoService().buscarTodos());
        } else {
            setJogos(getJogoService().buscarUsandoFiltro(filtro));
            setFiltro(filtro);
        }
        setEquipes(getEquipeService().buscarTodasEquipes());
        setTelaPalpites(true);
        prepararMapaPalpitesUsuario();
        return SUCCESS;
    }

    private void prepararMapaPalpitesUsuario() {
        String login = RequestUtils.getLoginParticipanteAutenticado();
        if (login == null) {
            return;
        }
        List palpitesDoUsuario = getPalpiteService().buscarPalpitesDoParticipante(login);
        if (palpitesDoUsuario == null) {
            return;
        }
        Map<Long, Palpite> mapa = new HashMap<>();
        for (Object objeto : palpitesDoUsuario) {
            Palpite palpite = (Palpite) objeto;
            if (palpite.getJogo() != null && palpite.getJogo().getId() != null) {
                mapa.put(palpite.getJogo().getId(), palpite);
            }
        }
        setPalpitesUsuario(mapa);
    }
	
    public String gerarGraficoDesempenho() {
        String login = RequestUtils.getLoginParticipanteAutenticado();
        Participante participante = getParticipanteService().buscarPorLogin(login);
        setParticipanteLogado(participante);
        List participantes = getParticipanteService().buscarClassificacao();
        participantes.remove(participante);
        Collections.sort(participantes, Participante.COMPARADOR_NOME);
        setParticipantes(participantes);
        return SUCCESS;
    }

    public String gerarGraficoDesempenhoImagem() {
        String login = RequestUtils.getLoginParticipanteAutenticado();
        Participante participante = getParticipanteService().buscarPorLogin(login);
        Long idRival = obterIdRival();
        GraficoComparativoDesempenho grafico = getParticipanteService().construirGraficoDesempenho(participante, idRival);
        JFreeChart chart = grafico.criarChart();
        this.graficoStream = renderizarChart(chart, 560, 240);
        return SUCCESS;
    }
    
    public String obterDadosPaginaPrincipal() {
        setJogosDeHoje(getJogoService().buscarJogosDeHoje());
        return SUCCESS;
    }

    public String gerarGraficoLiderancaImagem() {
        GraficoBarraLideres grafico = getParticipanteService().construirGraficoDeBarrasDosLideres();
        JFreeChart chart = grafico.criarChart();
        this.graficoStream = renderizarChart(chart, 560, 180);
        return SUCCESS;
    }

    private Long obterIdRival() {
        if (ValidacaoUtils.isVazia(this.rival)) {
            return null;
        }
        try {
            return Long.valueOf(this.rival);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private InputStream renderizarChart(JFreeChart chart, int largura, int altura) {
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ChartUtils.writeChartAsPNG(buffer, chart, largura, altura);
            return new ByteArrayInputStream(buffer.toByteArray());
        } catch (Exception ex) {
            throw new RuntimeException("Falha ao gerar grafico.", ex);
        }
    }

	private FiltroBuscaJogos obterFiltro() {
		FiltroBuscaJogos filtro = null;
        if (isUsarFiltro()) {
            filtro = new FiltroBuscaJogos();
    		filtro.setDataInicial(ConversaoUtils.converterParaData(getDataInicial()));
    		filtro.setDataFinal(ConversaoUtils.converterParaData(getDataFinal()));
            if (getFiltroFase() != null) {
                filtro.setFase(getFiltroFase());
            }
            if (getFiltroEquipe() != null) {
                filtro.setIdEquipe(getFiltroEquipe());
            }
    		filtro.setGrupo(getFiltroGrupo());
            filtro.setSoSemPalpite(isFiltroSemPalpite());
            filtro.setSoJogosQueNaoOcorreram(isFiltroJogosNaoOcorreram());
            filtro.setLogin(RequestUtils.getLoginParticipanteAutenticado());
        }
		return filtro;
	}
    
    private Participante obterParticipante() {
        Participante p = new Participante();
        p.setLogin(getLogin());
        p.setNome(getNome());
        p.setEmail(getEmail());
        p.setSenha(getSenha());
        p.setIp(RequestUtils.getIpDaRequisicao());
        return p;
    }

    // Getters e Setters anotados para Struts 7

    public String getLogin() { return login; }
    @StrutsParameter
    public void setLogin(String login) {
        this.loginPossuiHtml = SanitizationUtils.containsHtml(login);
        this.login = SanitizationUtils.cleanText(login, 32);
    }

    public String getNome() { return nome; }
    @StrutsParameter
    public void setNome(String nome) {
        this.nomePossuiHtml = SanitizationUtils.containsHtml(nome);
        this.nome = SanitizationUtils.cleanText(nome, 80);
    }

    public String getEmail() { return email; }
    @StrutsParameter
    public void setEmail(String email) {
        this.emailPossuiHtml = SanitizationUtils.containsHtml(email);
        this.email = SanitizationUtils.cleanText(email, 254);
    }

    public String getSenha() { return senha; }
    @StrutsParameter
    public void setSenha(String senha) {
        this.senha = senha == null ? null : senha.trim();
    }

    public boolean isUsarFiltro() { return usarFiltro; }
    @StrutsParameter
    public void setUsarFiltro(boolean usarFiltro) { this.usarFiltro = usarFiltro; }

    public String getDataInicial() { return dataInicial; }
    @StrutsParameter
    public void setDataInicial(String dataInicial) { this.dataInicial = dataInicial; }

    public String getDataFinal() { return dataFinal; }
    @StrutsParameter
    public void setDataFinal(String dataFinal) { this.dataFinal = dataFinal; }

    public Integer getFiltroFase() { return filtroFase; }
    @StrutsParameter
    public void setFiltroFase(Integer filtroFase) { this.filtroFase = filtroFase; }

    public Long getFiltroEquipe() { return filtroEquipe; }
    @StrutsParameter
    public void setFiltroEquipe(Long filtroEquipe) { this.filtroEquipe = filtroEquipe; }

    public String getFiltroGrupo() { return filtroGrupo; }
    @StrutsParameter
    public void setFiltroGrupo(String filtroGrupo) {
        this.filtroGrupo = SanitizationUtils.cleanText(filtroGrupo, 5);
    }

    public boolean isFiltroSemPalpite() { return filtroSemPalpite; }
    @StrutsParameter
    public void setFiltroSemPalpite(boolean filtroSemPalpite) { this.filtroSemPalpite = filtroSemPalpite; }

    public boolean isFiltroJogosNaoOcorreram() { return filtroJogosNaoOcorreram; }
    @StrutsParameter
    public void setFiltroJogosNaoOcorreram(boolean filtroJogosNaoOcorreram) { this.filtroJogosNaoOcorreram = filtroJogosNaoOcorreram; }
    
    public List buscarMeusPalpites() {
    	String login = RequestUtils.getLoginParticipanteAutenticado();
    	return getPalpiteService().buscarPalpitesDoParticipante(login);
    }
    
    public String listarMeusPalpitesHtmx() {
    	String login = RequestUtils.getLoginParticipanteAutenticado();
    	this.palpites = getPalpiteService().buscarPalpitesDoParticipante(login);
    	return SUCCESS;
    }

    public String listarPalpitesDoJogoHtmx() {
        if (this.jogoId == null) {
            this.palpites = Collections.emptyList();
            return SUCCESS;
        }
        this.palpites = getPalpiteService().buscarPalpitesDoJogo(this.jogoId);
        return SUCCESS;
    }

    public String atualizarPalpiteHtmx() {
        String login = RequestUtils.getLoginParticipanteAutenticado();
        if (login == null) {
            this.palpiteAtualizado = false;
            this.palpiteErro = null;
            return ERROR;
        }
        if (this.jogoId == null || this.palpiteGolsEquipe1 == null || this.palpiteGolsEquipe2 == null) {
            this.palpiteAtualizado = false;
            this.palpiteErro = null;
            return ERROR;
        }
        try {
            getPalpiteService().atualizarPalpite(login, this.jogoId, this.palpiteGolsEquipe1, this.palpiteGolsEquipe2, RequestUtils.getIpDaRequisicao());
            this.palpiteAtualizado = true;
            prepararMapaPalpitesUsuario();
            return SUCCESS;
        } catch (Exception ex) {
            this.palpiteAtualizado = false;
            this.palpiteErro = null;
            return ERROR;
        }
    }

	public String cadastrar() {
        Participante p = obterParticipante();
        if (!validarCadastroEntradas()) {
            setTentativaInclusao(p);
            return INPUT;
        }
        if (verificarDuplicidadeCadastro(p)) {
            setTentativaInclusao(p);
            return INPUT;
        }
        try {
            getParticipanteService().criarNovo(p);
            setSucessoCadastro(true);
            return SUCCESS;
        } catch (ValidacaoException e) {
            setTentativaInclusao(p);
            setErrosInclusao(e.getErros());
        }
		return INPUT;
	}
    
    public String buscarParticipantes() {
        this.participantes = getParticipanteService().buscarClassificacao();
        Collections.sort(this.participantes);
        return SUCCESS;
    }
	
	public boolean existeLogin(String login) {
		Participante participante = getParticipanteService().buscarPorLogin(login);
		if (participante == null) {
			return false;
		}
		return true;
	}
	
	public void atualizarPalpite(Long idJogo, Integer golsEquipe1, Integer golsEquipe2) {
		String login = RequestUtils.getLoginParticipanteAutenticado();
		String ip = RequestUtils.getIpDaRequisicao();
		getPalpiteService().atualizarPalpite(login, idJogo, golsEquipe1, golsEquipe2, ip);
	}
	
	public Palpite buscarPalpiteDoJogo(Long idJogo) {
		String login = RequestUtils.getLoginParticipanteAutenticado();
		return getPalpiteService().buscarPalpiteDoJogo(login, idJogo);
	}
    
    public List buscarPalpitesDoJogo(Long idJogo) {
        return getPalpiteService().buscarPalpitesDoJogo(idJogo);
    }
    
    public String gerarClassificacaoDaCopa() {
        return SUCCESS;
    }

	public PalpiteService getPalpiteService() {
		return palpiteService;
	}

	public void setPalpiteService(PalpiteService palpiteService) {
		this.palpiteService = palpiteService;
	}

	public JogoService getJogoService() {
		return jogoService;
	}

	public void setJogoService(JogoService jogoService) {
		this.jogoService = jogoService;
	}

    public List getJogos() {
        return jogos;
    }

    public void setJogos(List jogos) {
        this.jogos = jogos;
    }

    public Map<Long, Palpite> getPalpitesUsuario() {
        return palpitesUsuario;
    }

    public void setPalpitesUsuario(Map<Long, Palpite> palpitesUsuario) {
        this.palpitesUsuario = palpitesUsuario;
    }

	public boolean isTelaPalpites() {
		return telaPalpites;
	}

	public void setTelaPalpites(boolean telaPalpites) {
		this.telaPalpites = telaPalpites;
	}

	public ParticipanteService getParticipanteService() {
		return participanteService;
	}

    public void setParticipanteService(ParticipanteService participanteService) {
        this.participanteService = participanteService;
    }

    public List getParticipantes() {
        return this.participantes;
    }

    public List getPalpites() {
        return palpites;
    }

    public boolean isPalpiteAtualizado() {
        return palpiteAtualizado;
    }

    public String getPalpiteErro() {
        return palpiteErro;
    }

    public List getErrosInclusao() {
        return this.errosInclusao;
    }

    public void setErrosInclusao(List errosInclusao) {
        this.errosInclusao = errosInclusao;
    }

    public Participante getTentativaInclusao() {
        return this.tentativaInclusao;
    }

    public void setTentativaInclusao(Participante tentativaInclusao) {
        this.tentativaInclusao = tentativaInclusao;
    }
    
    public boolean isSucessoCadastro() {
        return this.sucessoCadastro;
    }

    public void setSucessoCadastro(boolean sucessoCadastro) {
        this.sucessoCadastro = sucessoCadastro;
    }

    private boolean validarCadastroEntradas() {
        boolean valido = true;
        List<MensagemErro> erros = new ArrayList<>();

        if (login == null || !SanitizationUtils.isValidLogin(login) || loginPossuiHtml) {
            registrarErro(erros, "signin.login", "cadastro.login.invalido");
            valido = false;
        }
        if (nome == null || nome.isBlank() || nome.length() < 3 || nomePossuiHtml) {
            registrarErro(erros, "signin.name", "cadastro.nome.invalido");
            valido = false;
        }
        if (email == null || !SanitizationUtils.isValidEmail(email) || emailPossuiHtml) {
            registrarErro(erros, "signin.email", "cadastro.email.invalido");
            valido = false;
        }
        if (!ValidacaoUtils.isSenhaValida(senha)) {
            registrarErro(erros, "signin.pwd", "cadastro.senha.invalida");
            valido = false;
        }

        this.errosInclusao = erros.isEmpty() ? null : erros;
        return valido;
    }

    private void registrarErro(List<MensagemErro> erros, String campoKey, String mensagemKey) {
        String campo = texto(campoKey, fallbackCampo(campoKey));
        String mensagem = texto(mensagemKey, fallbackMensagem(mensagemKey));
        erros.add(new MensagemErro(campo, mensagem, MensagemErro.SEVERIDADE_ERRO));
    }

    private boolean verificarDuplicidadeCadastro(Participante participante) {
        List<MensagemErro> duplicidades = new ArrayList<>();
        String loginNormalizado = participante.getLogin() == null ? null : participante.getLogin().trim().toLowerCase();
        if (loginNormalizado != null && !loginNormalizado.isBlank()) {
            Participante existente = getParticipanteService().buscarPorLogin(loginNormalizado);
            if (existente != null) {
                duplicidades.add(new MensagemErro(
                        texto("signin.login", "Login"),
                        texto("cadastro.login.duplicado", "Ja existe um cadastro ativo com este login."),
                        MensagemErro.SEVERIDADE_ERRO));
            }
        }
        String emailNormalizado = participante.getEmail() == null ? null : participante.getEmail().trim();
        if (emailNormalizado != null && !emailNormalizado.isBlank()) {
            Participante existenteEmail = getParticipanteService().buscarPorEmail(emailNormalizado);
            if (existenteEmail != null) {
                duplicidades.add(new MensagemErro(
                        texto("signin.email", "E-mail"),
                        texto("cadastro.email.duplicado", "Este e-mail ja esta associado a outro cadastro."),
                        MensagemErro.SEVERIDADE_ERRO));
            }
        }
        if (!duplicidades.isEmpty()) {
            if (this.errosInclusao == null) {
                this.errosInclusao = new ArrayList<>();
            }
            this.errosInclusao.addAll(duplicidades);
            return true;
        }
        return false;
    }

    private String texto(String key, String fallback) {
        try {
            String valor = getText(key);
            if (valor != null && !valor.isBlank() && !valor.equals(key)) {
                return valor;
            }
        } catch (Exception ignored) {
        }
        return fallback;
    }

    private String fallbackCampo(String key) {
        return switch (key) {
            case "signin.login" -> "Login";
            case "signin.name" -> "Nome";
            case "signin.email" -> "E-mail";
            case "signin.pwd" -> "Senha";
            default -> key;
        };
    }

    private String fallbackMensagem(String key) {
        return switch (key) {
            case "cadastro.login.invalido" -> "Login deve seguir o formato informado.";
            case "cadastro.nome.invalido" -> "Informe um nome completo válido, sem HTML.";
            case "cadastro.email.invalido" -> "Informe um e-mail válido no formato usuario@dominio.";
            case "cadastro.senha.invalida" -> "Informe uma senha entre 8 e 64 caracteres com símbolos seguros.";
            case "cadastro.login.duplicado" -> "Já existe um cadastro ativo com este login.";
            case "cadastro.email.duplicado" -> "Este e-mail já está associado a outro cadastro.";
            default -> key;
        };
    }

	public List getEquipes() {
		return equipes;
	}

	public void setEquipes(List equipes) {
		this.equipes = equipes;
	}

	public EquipeService getEquipeService() {
		return equipeService;
	}

	public void setEquipeService(EquipeService equipeService) {
		this.equipeService = equipeService;
	}

	public FiltroBuscaJogos getFiltro() {
		return filtro;
	}

	public void setFiltro(FiltroBuscaJogos filtro) {
		this.filtro = filtro;
	}

    
    public Participante getParticipanteLogado() {
        return this.participanteLogado;
    }
    
    public void setParticipanteLogado(Participante participanteLogado) {
        this.participanteLogado = participanteLogado;
    }
    
    public void setParticipantes(List participantes) {
        this.participantes = participantes;
    }
    
    public InputStream getGraficoStream() {
        return this.graficoStream;
    }

    public Long getJogoId() {
        return jogoId;
    }

    @StrutsParameter
    public void setJogoId(Long jogoId) {
        this.jogoId = jogoId;
    }

    @StrutsParameter
    public void setPalpiteGolsEquipe1(Integer palpiteGolsEquipe1) {
        this.palpiteGolsEquipe1 = palpiteGolsEquipe1;
    }

    @StrutsParameter
    public void setPalpiteGolsEquipe2(Integer palpiteGolsEquipe2) {
        this.palpiteGolsEquipe2 = palpiteGolsEquipe2;
    }

    public String getRival() {
        return rival;
    }

    @StrutsParameter
    public void setRival(String rival) {
        this.rival = rival;
    }

    public List getJogosDeHoje() {
        return this.jogosDeHoje;
    }

    public void setJogosDeHoje(List jogosDeHoje) {
        this.jogosDeHoje = jogosDeHoje;
    }

}
