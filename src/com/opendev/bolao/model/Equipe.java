package com.opendev.bolao.model;

import java.io.Serializable;
import java.util.List;

import com.opendev.bolao.util.FlagUtils;

public class Equipe implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Long id;
	private String nomePais;
	private Character grupo;
    private List jogos;

	public Character getGrupo() {
		return grupo;
	}

	public void setGrupo(Character grupo) {
		this.grupo = grupo;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNomePais() {
		return nomePais;
	}

	public void setNomePais(String nomePais) {
		this.nomePais = nomePais;
	}
    
    public List getJogos() {
        return this.jogos;
    }
    
    public void setJogos(List jogos) {
        this.jogos = jogos;
    }

	public String getCodigoPais() {
		return FlagUtils.countryCodeFromName(this.nomePais);
	}

	public String getEmojiBandeira() {
		return FlagUtils.emojiFromCountryCode(getCodigoPais());
	}

	public String getSiglaPais() {
		return FlagUtils.fallbackAcronym(getCodigoPais(), this.nomePais);
	}

	public boolean hasBandeira() {
		return FlagUtils.hasAssetForCountry(this.nomePais);
	}

	public String getBandeiraUrl() {
		return FlagUtils.assetPathForCountry(this.nomePais).orElse("");
	}
}
