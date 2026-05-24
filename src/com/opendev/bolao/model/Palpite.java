package com.opendev.bolao.model;

import java.io.Serializable;
import java.sql.Timestamp;
import jakarta.persistence.*;

import com.opendev.bolao.util.ConversaoUtils;
import com.opendev.bolao.util.DadosClassificacao;

/**
 * Representa um palpite feito por um participante em um jogo.
 * Mapeado para a tabela PAL_PALPITE com chave composta (Participante, Jogo).
 */
@Entity
@Table(name = "PAL_PALPITE")
@IdClass(PalpiteId.class)
public class Palpite implements Serializable, Comparable {

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "PAL_PAR_ID")
	private Long idParticipante;

	@Id
	@Column(name = "PAL_JOG_ID")
	private Long idJogo;

	@Column(name = "PAL_EQP1_GOLS", nullable = false)
	private Integer golsEquipe1;

	@Column(name = "PAL_EQP2_GOLS", nullable = false)
	private Integer golsEquipe2;

	@Column(name = "PAL_IP", nullable = false, length = 45)
	private String ip;

	@Column(name = "PAL_DH_ATUALIZACAO", nullable = false)
	private Timestamp dataHoraAtualizacao;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "PAL_PAR_ID", insertable = false, updatable = false)
	private Participante participante;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "PAL_JOG_ID", insertable = false, updatable = false)
	private Jogo jogo;

	@Transient
	private DadosClassificacao dadosClassificacao;

	public Integer getGolsEquipe1() {
		return golsEquipe1;
	}

	public void setGolsEquipe1(Integer golsEquipe1) {
		this.golsEquipe1 = golsEquipe1;
	}

	public Integer getGolsEquipe2() {
		return golsEquipe2;
	}

	public void setGolsEquipe2(Integer golsEquipe2) {
		this.golsEquipe2 = golsEquipe2;
	}

	public Jogo getJogo() {
		return jogo;
	}

	public void setJogo(Jogo jogo) {
		this.jogo = jogo;
	}

	public Participante getParticipante() {
		return participante;
	}

	public void setParticipante(Participante participante) {
		this.participante = participante;
	}

	public Long getIdJogo() {
		return idJogo;
	}

	public void setIdJogo(Long idJogo) {
		this.idJogo = idJogo;
	}

	public Long getIdParticipante() {
		return idParticipante;
	}

	public void setIdParticipante(Long idParticipante) {
		this.idParticipante = idParticipante;
	}
	
	public boolean apostouNaEquipe1() {
		return getGolsEquipe1().compareTo(getGolsEquipe2()) > 0;
	}
	
	public boolean apostouNaEquipe2() {
		return getGolsEquipe1().compareTo(getGolsEquipe2()) < 0;
	}
	
	public boolean apostouNoEmpate() {
		return getGolsEquipe1().compareTo(getGolsEquipe2()) == 0;
	}
	
	public Timestamp getDataHoraAtualizacao() {
		return dataHoraAtualizacao;
	}

	public void setDataHoraAtualizacao(Timestamp dataHoraAtualizacao) {
		this.dataHoraAtualizacao = dataHoraAtualizacao;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public DadosClassificacao getPontuacao() {
		int pontos = 0;
		DadosClassificacao dados = this.dadosClassificacao;
		if (dados != null) {
			return dados;
		}
		Jogo jogo = getJogo();
		if (jogo.jaOcorreu() && jogo.jaFoiAtualizado()) {
			dados = new DadosClassificacao();
			Integer golsJogoEquipe1 = getJogo().getGolsEquipe1();
			Integer golsJogoEquipe2 = getJogo().getGolsEquipe2();
			if ((golsJogoEquipe1.compareTo(getGolsEquipe1()) == 0)
					&& (golsJogoEquipe2.compareTo(getGolsEquipe2()) == 0)) {
				dados.setQuantidadeDeAcertosTotais(1);
				pontos = 6;
			} else {
				if ((golsJogoEquipe1.compareTo(getGolsEquipe1()) == 0)
						|| (golsJogoEquipe2.compareTo(getGolsEquipe2()) == 0)) {
					pontos = 1;
				}
				if ((jogo.foiEmpate() && apostouNoEmpate())
						|| (jogo.foiEquipe1Vencedora() && apostouNaEquipe1())
						|| (jogo.foiEquipe2Vencedora() && apostouNaEquipe2())) {
					pontos += 2;
					if (pontos == 3) {
						dados.setQuantidadeDeAcertosParciaisComBonus(1);
					} else {
						dados.setQuantidadeDeAcertosParciais(1);
					}
				}
			}
			if (pontos == 1) {
				dados.setQuantidadeSoBonus(1);
			} else if (pontos == 0) {
				dados.setQuantidadeDeErros(1);
			}

			dados.setPontuacao(pontos);
		}
		this.dadosClassificacao = dados;
		return dados;
	}
	
	
	// Getters de atalho
	
	public String getNomeParticipante() {
		return getParticipante().getNomeFormatado();
	}
	
	public String getRepresentacaoPalpite() {
		return getGolsEquipe1() + " X " + getGolsEquipe2();
	}
	
	public String getPontos() {
		DadosClassificacao dados = getPontuacao();
		if (dados == null) {
			return "";
		}
		return Integer.toString(dados.getPontuacao());
	}

	public int compareTo(Object o) {
		Palpite outro = (Palpite) o;
        Participante esse = this.getParticipante();
        Participante outroParticipante = outro.getParticipante();
		return esse.getNomeFormatado().compareToIgnoreCase(outroParticipante.getNomeFormatado());
	}
	
	public String getDataDoJogo() {
		return ConversaoUtils.converterParaString(getJogo().getData());
	}
	
	public String getHoraDoJogo() {
		return ConversaoUtils.converterParaString(getJogo().getHora());
	}
	
    public String getPaisEquipe1() {
        return getJogo().getEquipe1().getNomePais();
    }
    
    public String getPaisEquipe2() {
        return getJogo().getEquipe2().getNomePais();
    }
	
}
