package com.opendev.bolao.util;

import java.util.Date;

import org.hibernate.Query;

public class FiltroBuscaJogos implements FiltroBusca {
	
	public static final String FILTRO_JOGOS_SESSION = "com.opendev.bolao.util.filtroBuscaJogos";
	
	private boolean soSemPalpite;
	private Integer fase;
	private String grupo;
	private Long idEquipe;
	private Date dataInicial;
	private Date dataFinal;
	private String login;
    private boolean soJogosQueNaoOcorreram;
	
	public String getHqlQuery() {
		StringBuffer query = new StringBuffer();
		query.append("from Jogo as j where 1 = 1 ");
		if (dataInicial != null) {
			if (dataFinal != null) {
				query.append("and (j.data between :dataInicial and :dataFinal) ");
			} else {
				query.append("and (j.data >= :dataInicial) ");
			}
		} else if (dataFinal != null) {
			query.append("and (j.data <= :dataFinal) ");
		}
		if (fase != null) {
			query.append("and (j.fase = :fase) ");
		}
        if (getIdEquipe() != null) {
            query.append("and (j.equipe1.id = :equipe or j.equipe2.id = :equipe) ");
        }
		if (!ValidacaoUtils.isVazia(grupo)) {
			query.append("and (j.fase in (11,12,13) and (j.equipe1.grupo = :grupo or j.equipe2.grupo = :grupo)) ");
		}
		if (isSoSemPalpite()) {
			query.append("and (j not in (select p.jogo from Palpite as p where p.participante.login = :login)) ");
		}
        if (isSoJogosQueNaoOcorreram()) {
            query.append("and (j.golsEquipe1 is null and j.golsEquipe2 is null) ");
        }
		query.append("order by j.data asc, j.hora asc");
//		System.out.println(query.toString());
		return query.toString();
	}
	
	public Query popularParametrosDaHql(Query query) {
		if (dataInicial != null) {
			query.setDate("dataInicial", getDataInicial());
		}
		if (dataFinal != null) {
			query.setDate("dataFinal", getDataInicial());
		}
		if (fase != null) {
			query.setInteger("fase", getFase().intValue());
		}
        if (getIdEquipe() != null) {
            query.setLong("equipe", getIdEquipe().longValue());
        }
		if (!ValidacaoUtils.isVazia(grupo)) {
			query.setString("grupo", getGrupo().toUpperCase());
		}
		if (soSemPalpite) {
			query.setString("login", getLogin());
		}
		return query;
	}


	public Date getDataFinal() {
		return dataFinal;
	}

	public void setDataFinal(Date dataFinal) {
		this.dataFinal = dataFinal;
	}

	public Date getDataInicial() {
		return dataInicial;
	}

	public void setDataInicial(Date dataInicial) {
		this.dataInicial = dataInicial;
	}

	public Integer getFase() {
		return fase;
	}

	public void setFase(Integer fase) {
		this.fase = fase;
	}

	public String getGrupo() {
		return grupo;
	}

	public void setGrupo(String grupo) {
		this.grupo = grupo;
	}

	public Long getIdEquipe() {
		return idEquipe;
	}

	public void setIdEquipe(Long idEquipe) {
		this.idEquipe = idEquipe;
	}

	public boolean isSoSemPalpite() {
		return soSemPalpite;
	}

	public void setSoSemPalpite(boolean soSemPalpite) {
		this.soSemPalpite = soSemPalpite;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}
	
	public String getDataInicialFormatada() {
		return getDataInicial() == null ?
				null : ConversaoUtils.converterParaString(getDataInicial());
	}
	
	public String getDataFinalFormatada() {
		return getDataFinal() == null ?
				null : ConversaoUtils.converterParaString(getDataFinal());
	}
    
    public boolean isSoJogosQueNaoOcorreram() {
        return this.soJogosQueNaoOcorreram;
    }
    
    public void setSoJogosQueNaoOcorreram(boolean soJogosQueNaoOcorreram) {
        this.soJogosQueNaoOcorreram = soJogosQueNaoOcorreram;
    }


}
