package com.opendev.bolao.model;

import java.io.Serializable;
import java.util.List;
import jakarta.persistence.*;

import com.opendev.bolao.util.FlagUtils;

/**
 * Representa uma equipe (país) participante da Copa.
 * Mapeado para a tabela EQP_EQUIPE via Spring Data JPA.
 */
@Entity
@Table(name = "EQP_EQUIPE")
public class Equipe implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "EQP_ID")
	private Long id;

	@Column(name = "EQP_PAIS", nullable = false, length = 100)
	private String nomePais;

	@Column(name = "EQP_GRUPO", nullable = false)
	private Character grupo;

    @OneToMany(mappedBy = "equipe1", fetch = FetchType.LAZY)
    private List<Jogo> jogos;

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
    
    public List<Jogo> getJogos() {
        return this.jogos;
    }
    
    public void setJogos(List<Jogo> jogos) {
        this.jogos = jogos;
    }

	public String getCodigoPais() {
		return FlagUtils.countryCodeFromName(getNomePais());
	}

	public String getEmojiBandeira() {
		return FlagUtils.emojiFromCountryCode(getCodigoPais());
	}

	public String getSiglaPais() {
		return FlagUtils.fallbackAcronym(getCodigoPais(), getNomePais());
	}

	public boolean hasBandeira() {
		return FlagUtils.hasAssetForCountry(getNomePais());
	}

	public String getBandeiraUrl() {
		return FlagUtils.assetPathForCountry(getNomePais()).orElse("");
	}
}
