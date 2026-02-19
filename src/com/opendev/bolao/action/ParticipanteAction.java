package com.opendev.bolao.action;

import java.util.Collections;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import com.opendev.bolao.exception.ValidacaoException;
/*
import com.opendev.bolao.grafico.GraficoBarraLideres;
import com.opendev.bolao.grafico.GraficoComparativoDesempenho;
*/
import com.opendev.bolao.model.Palpite;
import com.opendev.bolao.model.Participante;
import com.opendev.bolao.service.EquipeService;
import com.opendev.bolao.service.JogoService;
import com.opendev.bolao.service.PalpiteService;
import com.opendev.bolao.service.ParticipanteService;
import com.opendev.bolao.util.ConversaoUtils;
import com.opendev.bolao.util.FiltroBuscaJogos;
import com.opendev.bolao.util.RequestUtils;
import com.opendev.bolao.util.ValidacaoUtils;
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
    private List errosInclusao;
    private Participante tentativaInclusao;
    private boolean sucessoCadastro;
    private List participantes;
    private List equipes;
    private FiltroBuscaJogos filtro;
//    private GraficoComparativoDesempenho grafico;
    private Participante participanteLogado;
    
    // Dados página principal
    private List jogosDeHoje;
//    private GraficoBarraLideres graficoLideranca;

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
		return SUCCESS;
	}
	
/*
	public String gerarGraficoDesempenho() {
		String login = RequestUtils.getLoginParticipanteAutenticado();
		HttpServletRequest request = RequestUtils.getRequest();
		String rival = request.getParameter("rival");
		Long idRival = null;
		if (!ValidacaoUtils.isVazia(rival)) {
            idRival = Long.valueOf(rival);
        }
        Participante participante = getParticipanteService().buscarPorLogin(login);
		GraficoComparativoDesempenho grafico = getParticipanteService().construirGraficoDesempenho(participante, idRival);
		setGrafico(grafico);
        setParticipanteLogado(participante);
        List participantes = getParticipanteService().buscarClassificacao();
        participantes.remove(participante);
        Collections.sort(participantes, Participante.COMPARADOR_NOME);
        setParticipantes(participantes);
		return SUCCESS;
	}
*/
    public String gerarGraficoDesempenho() {
        return SUCCESS;
    }
    
/*
    public String obterDadosPaginaPrincipal() {
        setJogosDeHoje(getJogoService().buscarJogosDeHoje());
        setGraficoLideranca(getParticipanteService().construirGraficoDeBarrasDosLideres());
        return SUCCESS;
    }
*/
    public String obterDadosPaginaPrincipal() {
        setJogosDeHoje(getJogoService().buscarJogosDeHoje());
        // setGraficoLideranca(getParticipanteService().construirGraficoDeBarrasDosLideres());
        return SUCCESS;
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
    public void setLogin(String login) { this.login = login; }

    public String getNome() { return nome; }
    @StrutsParameter
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    @StrutsParameter
    public void setEmail(String email) { this.email = email; }

    public String getSenha() { return senha; }
    @StrutsParameter
    public void setSenha(String senha) { this.senha = senha; }

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
    public void setFiltroGrupo(String filtroGrupo) { this.filtroGrupo = filtroGrupo; }

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

	public String cadastrar() {
        Participante p = obterParticipante();
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

/*
	public GraficoComparativoDesempenho getGrafico() {
		return grafico;
	}

	public void setGrafico(GraficoComparativoDesempenho grafico) {
		this.grafico = grafico;
	}
*/
    
    public Participante getParticipanteLogado() {
        return this.participanteLogado;
    }
    
    public void setParticipanteLogado(Participante participanteLogado) {
        this.participanteLogado = participanteLogado;
    }
    
    public void setParticipantes(List participantes) {
        this.participantes = participantes;
    }
    
/*
    public GraficoBarraLideres getGraficoLideranca() {
        return this.graficoLideranca;
    }

    public void setGraficoLideranca(GraficoBarraLideres graficoLideranca) {
        this.graficoLideranca = graficoLideranca;
    }
*/

    public List getJogosDeHoje() {
        return this.jogosDeHoje;
    }

    public void setJogosDeHoje(List jogosDeHoje) {
        this.jogosDeHoje = jogosDeHoje;
    }

}
