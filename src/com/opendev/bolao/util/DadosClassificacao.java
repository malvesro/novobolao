package com.opendev.bolao.util;

import java.io.Serializable;
import java.text.DecimalFormat;

public class DadosClassificacao implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#00.00");

	private String loginParticipante;
	private String nomeParticipante;
	private int quantidadeDeAcertosTotais;
	private int quantidadeDeAcertosParciais;
	private int quantidadeDeAcertosParciaisComBonus;
	private int quantidadeDeErros;
    private int quantidadeSoBonus;
    private int pontuacao;
    private int totalDeJogos;

	
    public String getLoginParticipante() {
        return this.loginParticipante;
    }

    public void setLoginParticipante(String loginParticipante) {
        this.loginParticipante = loginParticipante;
    }

    public int getQuantidadeDeAcertosParciais() {
		return quantidadeDeAcertosParciais;
	}

	public void setQuantidadeDeAcertosParciais(int quantidadeDeAcertosParciais) {
		this.quantidadeDeAcertosParciais = quantidadeDeAcertosParciais;
	}

	public int getQuantidadeDeAcertosParciaisComBonus() {
		return quantidadeDeAcertosParciaisComBonus;
	}

    public void setQuantidadeDeAcertosParciaisComBonus(int quantidadeDeAcertosParciaisComBonus) {
        this.quantidadeDeAcertosParciaisComBonus = quantidadeDeAcertosParciaisComBonus;
    }

    public int getQuantidadeDeAcertosTotais() {
        return quantidadeDeAcertosTotais;
    }

    public void setQuantidadeDeAcertosTotais(int quantidadeDeAcertosTotais) {
        this.quantidadeDeAcertosTotais = quantidadeDeAcertosTotais;
    }

    public int getQuantidadeDeErros() {
        return quantidadeDeErros;
    }

    public void setQuantidadeDeErros(int quantidadeDeErros) {
        this.quantidadeDeErros = quantidadeDeErros;
    }

    public int getPontuacao() {
        return this.pontuacao;
    }

    public void setPontuacao(int quantidadeDePontos) {
        this.pontuacao = quantidadeDePontos;
    }

    public int getQuantidadeSoBonus() {
        return this.quantidadeSoBonus;
    }

    public void setQuantidadeSoBonus(int quantidadeSoBonus) {
        this.quantidadeSoBonus = quantidadeSoBonus;
    }

	public String getNomeParticipante() {
		return nomeParticipante;
	}

	public void setNomeParticipante(String nomeParticipante) {
		this.nomeParticipante = nomeParticipante;
	}

	public int getTotalDeJogos() {
		return totalDeJogos;
	}

	public void setTotalDeJogos(int totalDeJogos) {
		this.totalDeJogos = totalDeJogos;
	}
	
	public int getTotalPalpitesConsiderados() {
		return getQuantidadeDeAcertosParciais() + getQuantidadeDeAcertosParciaisComBonus() +
			getQuantidadeDeAcertosTotais() + getQuantidadeDeErros() +
			getQuantidadeSoBonus();
	}

	public String getAproveitamento() {
		double aprCalc = (getPontuacao() / (getTotalDeJogos() * 6.00)) * 100.00;
		String apr = DECIMAL_FORMAT.format(aprCalc) + "%";		
		return apr;
	}

}
