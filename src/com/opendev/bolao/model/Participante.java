package com.opendev.bolao.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.opendev.bolao.exception.ValidacaoException;
import com.opendev.bolao.util.Cache;
import com.opendev.bolao.util.DadosClassificacao;
import com.opendev.bolao.util.MensagemErro;
import com.opendev.bolao.util.StringUtils;
import com.opendev.bolao.util.ValidacaoUtils;
import com.opendev.bolao.util.SanitizationUtils;

public class Participante implements Serializable, Comparable {

	private static final long serialVersionUID = 1L;
	private static final Cache CACHE_DADOS_CLASSIFICACAO = new Cache();
    public static final Comparator COMPARADOR_NOME = new Participante.ComparadorNome();

	private Long id;
	private String nome;
	private String login;
	private String senha;
	private String email;
    private transient boolean loginPossuiMarkup;
    private transient boolean nomePossuiMarkup;
    private transient boolean emailPossuiMarkup;
	private boolean habilitado;
    private String ip;
    private Timestamp dataHoraCadastro;
	private Set palpites;
	private Set privilegios;
	private DadosClassificacao pontuacaoTotal;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.emailPossuiMarkup = this.emailPossuiMarkup || SanitizationUtils.containsHtml(email);
		this.email = SanitizationUtils.cleanText(email, 254);
	}

	public boolean isLoginPossuiMarkup() {
		return loginPossuiMarkup;
	}

	public boolean isNomePossuiMarkup() {
		return nomePossuiMarkup;
	}

	public boolean isEmailPossuiMarkup() {
		return emailPossuiMarkup;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.loginPossuiMarkup = this.loginPossuiMarkup || SanitizationUtils.containsHtml(login);
		this.login = SanitizationUtils.cleanText(login, 32);
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nomePossuiMarkup = this.nomePossuiMarkup || SanitizationUtils.containsHtml(nome);
		this.nome = SanitizationUtils.cleanText(nome, 80);
	}

	public boolean isHabilitado() {
		return habilitado;
	}

	public void setHabilitado(boolean pago) {
		this.habilitado = pago;
	}

	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha == null ? null : senha.trim();
	}

	public Set getPalpites() {
		return palpites;
	}

	public void setPalpites(Set palpites) {
		this.palpites = palpites;
	}

	public Set getPrivilegios() {
		return privilegios;
	}

	public void setPrivilegios(Set papeis) {
		this.privilegios = papeis;
	}
	
    public Timestamp getDataHoraCadastro() {
        return this.dataHoraCadastro;
    }
    
    public void setDataHoraCadastro(Timestamp dataHoraCadastro) {
        this.dataHoraCadastro = dataHoraCadastro;
    }
    
    public String getIp() {
        return this.ip;
    }
    
    public void setIp(String ip) {
        this.ip = ip;
    }

    public void addPapel(String papel) {
		if (this.privilegios == null) {
			this.privilegios = new HashSet();
		}
		Privilegio privilegio = new Privilegio();
		privilegio.setPapel(papel);
		this.privilegios.add(privilegio);
	}
    
    public Privilegio getPrivilegio() {
        Privilegio papel = null;
        Set privilegios = getPrivilegios();
        if (privilegios != null && !privilegios.isEmpty()) {
            papel = (Privilegio) privilegios.iterator().next();
        } else {
            papel = new Privilegio();
            papel.setIdParticipante(getId());
            papel.setPapel("");
            privilegios = new HashSet();
            privilegios.add(papel);
            setPrivilegios(privilegios);
        }
        return papel;
    }
	
	public static void expirarCacheDeClassificacao() {
		CACHE_DADOS_CLASSIFICACAO.setExpirado(true);
	}
    
    public static void notificarCacheAtualizado() {
        CACHE_DADOS_CLASSIFICACAO.setExpirado(false);
    }
	
	public DadosClassificacao getPontuacaoTotal() {
		DadosClassificacao totais = this.pontuacaoTotal;
		if (totais != null) {
			return totais;
		}
		synchronized (CACHE_DADOS_CLASSIFICACAO) {
			if (CACHE_DADOS_CLASSIFICACAO.isExpirado()) {
				totais = calcularTotais();
			} else {
				totais = (DadosClassificacao) CACHE_DADOS_CLASSIFICACAO.get(getId());
				if (totais == null) {
					totais = calcularTotais();
				}
			}
		}
		this.pontuacaoTotal = totais;
        return totais;
	}

    private DadosClassificacao calcularTotais() {
    	DadosClassificacao totais = null;
		Iterator iter = getPalpites().iterator();
		Palpite palpite = null;
		DadosClassificacao dadosPalpite = null;
		int totaisAcertosParciais = 0;
		int totaisAcertosParciaisComBonus = 0;
		int totaisAcertosTotais = 0;
		int totaisErros = 0;
		int totaisSoBonus = 0;
		int totaisPontos = 0;
		while (iter.hasNext()) {
			palpite = (Palpite) iter.next();
			dadosPalpite = palpite.getPontuacao();
			if (dadosPalpite != null) {
				totaisAcertosParciais += dadosPalpite.getQuantidadeDeAcertosParciais();
				totaisAcertosParciaisComBonus += dadosPalpite.getQuantidadeDeAcertosParciaisComBonus();
				totaisAcertosTotais += dadosPalpite.getQuantidadeDeAcertosTotais();
				totaisErros += dadosPalpite.getQuantidadeDeErros();
				totaisSoBonus += dadosPalpite.getQuantidadeSoBonus();
				totaisPontos += dadosPalpite.getPontuacao();
			}
		}
		totais = new DadosClassificacao();
		totais.setLoginParticipante(getLogin());
		totais.setNomeParticipante(getNomeFormatado());
		totais.setQuantidadeDeAcertosParciais(totaisAcertosParciais);
		totais.setQuantidadeDeAcertosParciaisComBonus(totaisAcertosParciaisComBonus);
		totais.setQuantidadeDeAcertosTotais(totaisAcertosTotais);
		totais.setQuantidadeDeErros(totaisErros);
		totais.setQuantidadeSoBonus(totaisSoBonus);
		totais.setPontuacao(totaisPontos);
		CACHE_DADOS_CLASSIFICACAO.put(getId(), totais);
		return totais;
	}

	public int compareTo(Object o) {
        Participante outro = (Participante) o;
        int comparacaoPontuacao = outro.getPontuacaoTotal().getPontuacao() - this.getPontuacaoTotal().getPontuacao();
        int comparacaoNomes = this.getNomeFormatado().compareToIgnoreCase(outro.getNomeFormatado());
        return (comparacaoPontuacao * 100) + comparacaoNomes;
    }
    
    public String getNomeFormatado() {
        return StringUtils.formatarNomeCompleto(getNome());
    }

    public void validar() throws ValidacaoException {
        List erros = new ArrayList();
        final String campoObrigatorio = "Campo obrigatório!";
        if (ValidacaoUtils.isVazia(getLogin())) {
            erros.add(new MensagemErro("Login", campoObrigatorio, MensagemErro.SEVERIDADE_AVISO));
        } else {
            if (isLoginPossuiMarkup()) {
                erros.add(new MensagemErro("Login", "Conteudo invalido (HTML nao permitido).", MensagemErro.SEVERIDADE_ERRO));
            } else if (!SanitizationUtils.isValidLogin(getLogin())) {
                erros.add(new MensagemErro("Login", "Informe um login com 3 a 32 caracteres (letras, numeros, ponto, hifen ou underline).", MensagemErro.SEVERIDADE_ERRO));
            }
        }

        if (ValidacaoUtils.isVazia(getEmail())) {
            erros.add(new MensagemErro("E-mail", campoObrigatorio, MensagemErro.SEVERIDADE_AVISO));
        } else {
            if (isEmailPossuiMarkup()) {
                erros.add(new MensagemErro("E-mail", "Conteudo invalido (HTML nao permitido).", MensagemErro.SEVERIDADE_ERRO));
            } else if (!SanitizationUtils.isValidEmail(getEmail())) {
                erros.add(new MensagemErro("E-mail", "Formato invalido!", MensagemErro.SEVERIDADE_ERRO));
            }
        }

        if (ValidacaoUtils.isVazia(getNome())) {
            erros.add(new MensagemErro("Nome", campoObrigatorio, MensagemErro.SEVERIDADE_AVISO));
        } else {
            if (isNomePossuiMarkup()) {
                erros.add(new MensagemErro("Nome", "Conteudo invalido (HTML nao permitido).", MensagemErro.SEVERIDADE_ERRO));
            } else if (getNome().indexOf(" ") == -1) {
                erros.add(new MensagemErro("Nome", "Informe pelo menos um sobrenome", MensagemErro.SEVERIDADE_ERRO));
            }
        }

        if (ValidacaoUtils.isVazia(getSenha())) {
            erros.add(new MensagemErro("Senha", campoObrigatorio, MensagemErro.SEVERIDADE_AVISO));
        } else if (!ValidacaoUtils.isSenhaValida(getSenha())) {
            erros.add(new MensagemErro("Senha", "Deve ter entre 8 e 64 caracteres sem usar caracteres de controle.", MensagemErro.SEVERIDADE_ERRO));
        }
        
        if (!erros.isEmpty()) {
            throw new ValidacaoException(erros);
        }
    }
    
    private static class ComparadorNome implements Comparator {

        public int compare(Object o1, Object o2) {
            Participante umParticipante = (Participante) o1;
            Participante outroParticipante = (Participante) o2;
            return umParticipante.getNomeFormatado().compareToIgnoreCase(outroParticipante.getNomeFormatado());
        }
        
    }

}
