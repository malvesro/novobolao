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
import com.opendev.bolao.util.SanitizationUtils;
import com.opendev.bolao.util.StringUtils;
import com.opendev.bolao.util.ValidacaoUtils;

import com.opendev.bolao.util.jpa.BooleanCharConverter;

import jakarta.persistence.*;

/**
 * Representa um participante do bolão.
 * Mapeado para a tabela PAR_PARTICIPANTE via Spring Data JPA.
 */
@Entity
@Table(name = "PAR_PARTICIPANTE")
public class Participante implements Serializable, Comparable {

	private static final long serialVersionUID = 1L;
	private static final Cache CACHE_DADOS_CLASSIFICACAO = new Cache();
    public static final Comparator COMPARADOR_NOME = new Participante.ComparadorNome();

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "PAR_ID")
	private Long id;

	@Column(name = "PAR_NOME", nullable = false, length = 80)
	private String nome;

	@Column(name = "PAR_LOGIN", nullable = false, unique = true, length = 32)
	private String login;

	@Column(name = "PAR_SENHA", nullable = false)
	private String senha;

	@Column(name = "PAR_EMAIL", nullable = false, length = 254)
	private String email;

    private transient boolean loginPossuiMarkup;
    private transient boolean nomePossuiMarkup;
    private transient boolean emailPossuiMarkup;

	@Convert(converter = BooleanCharConverter.class)
	@Column(name = "PAR_HABILITADO", nullable = false, columnDefinition = "char(1)", length = 1)
	private boolean habilitado;

	@Column(name = "PAR_IP", nullable = false, length = 45)
    private String ip;

	@Column(name = "PAR_DH_CADASTRO", nullable = false)
	   private Timestamp dataHoraCadastro;

	@Column(name = "PAR_DH_ULTIMA_TROCA_SENHA")
	private Timestamp dataHoraUltimaTrocaSenha;

	@OneToMany(mappedBy = "participante", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
	private Set<Palpite> palpites;

	@OneToMany(mappedBy = "idParticipante", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<Privilegio> privilegios;

	@Transient
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

    public Timestamp getDataHoraUltimaTrocaSenha() {
        return dataHoraUltimaTrocaSenha;
    }

    public void setDataHoraUltimaTrocaSenha(Timestamp dataHoraUltimaTrocaSenha) {
        this.dataHoraUltimaTrocaSenha = dataHoraUltimaTrocaSenha;
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
		privilegio.setIdParticipante(this.id);
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

	/**
	 * Verifica se o participante possui o perfil de administrador (ROLE_ADMIN).
	 * @return true se for administrador, false caso contrário.
	 */
	public boolean isAdministrador() {
		Set s = getPrivilegios();
		if (s == null || s.isEmpty()) {
			return false;
		}
		for (Object o : s) {
			if (o instanceof Privilegio) {
				Privilegio p = (Privilegio) o;
				if ("ROLE_ADMIN".equalsIgnoreCase(p.getPapel())) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static void expirarCacheDeClassificacao() {
		CACHE_DADOS_CLASSIFICACAO.setExpirado(true);
	}
    
    public static boolean isCacheExpirado() {
        return CACHE_DADOS_CLASSIFICACAO.isExpirado();
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
        DadosClassificacao minhaPontuacao = this.getPontuacaoTotal();
        DadosClassificacao outraPontuacao = outro.getPontuacaoTotal();

        // Ordem oficial de desempate do ranking (conforme regras públicas):
        // 1) Pontuação total (desc)
        // 2) Quantidade de acertos totais - 6 pontos (desc)
        // 3) Quantidade de acertos parciais com bônus - 3 pontos (desc)
        // 4) Nome formatado (asc, case-insensitive)
        int comparacaoPontuacao = outraPontuacao.getPontuacao() - minhaPontuacao.getPontuacao();
        if (comparacaoPontuacao != 0) {
            return comparacaoPontuacao;
        }

        int comparacaoAcertosTotais = outraPontuacao.getQuantidadeDeAcertosTotais() - minhaPontuacao.getQuantidadeDeAcertosTotais();
        if (comparacaoAcertosTotais != 0) {
            return comparacaoAcertosTotais;
        }

        int comparacaoAcertosParciaisBonus = outraPontuacao.getQuantidadeDeAcertosParciaisComBonus()
                - minhaPontuacao.getQuantidadeDeAcertosParciaisComBonus();
        if (comparacaoAcertosParciaisBonus != 0) {
            return comparacaoAcertosParciaisBonus;
        }

        return this.getNomeFormatado().compareToIgnoreCase(outro.getNomeFormatado());
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
