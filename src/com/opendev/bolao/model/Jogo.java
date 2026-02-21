package com.opendev.bolao.model;

import java.io.Serializable;
import java.sql.Time;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.opendev.bolao.util.ConversaoUtils;

public class Jogo implements Serializable, Comparable {

	private static final long serialVersionUID = 1L;

	public static final int FASE_FINAL = 1;
	public static final int FASE_TERCEIRO_LUGAR = 3;
	public static final int FASE_SEMIFINAL = 2;
	public static final int FASE_QUARTAS = 4;
	public static final int FASE_OITAVAS = 8;
	public static final int FASE_TRINTA_DOIS_AVOS = 16;
	public static final int FASE_GRUPO_RODADA_1 = 11;
	public static final int FASE_GRUPO_RODADA_2 = 12;
	public static final int FASE_GRUPO_RODADA_3 = 13;
	
	private Long id;
	private Date data;
	private Time hora;
	private String local;
	private Integer golsEquipe1;
	private Integer golsEquipe2;
	private int fase;
	private Equipe equipe1;
	private Equipe equipe2;
	
	private transient Calendar dataHora;

	public Date getData() {
		return data;
	}

	public void setData(Date data) {
		this.data = data;
	}

	public Time getHora() {
		return hora;
	}

	public void setHora(Time hora) {
		this.hora = hora;
	}

	public Equipe getEquipe1() {
		return equipe1;
	}

	public void setEquipe1(Equipe equipe1) {
		this.equipe1 = equipe1;
	}

	public Equipe getEquipe2() {
		return equipe2;
	}

	public void setEquipe2(Equipe equipe2) {
		this.equipe2 = equipe2;
	}

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

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getLocal() {
		return local;
	}

	public void setLocal(String local) {
		this.local = local;
	}

	public Calendar getDataHora() {
		if (this.dataHora == null) {
			Calendar dh = new GregorianCalendar();
			dh.clear();
			dh.setTime(this.getData());
			dh.set(Calendar.HOUR_OF_DAY, ConversaoUtils.converterHoraParaInteiro(this.getHora()));
			this.dataHora = dh;
		}
		return this.dataHora;
	}

	public int compareTo(Object o) {
		Jogo other = (Jogo) o;		
		return this.getDataHora().compareTo(other.getDataHora());
	}
	
	public boolean foiEmpate() {
		return jaOcorreu() && this.golsEquipe1.compareTo(this.golsEquipe2) == 0;
	}
	
	public boolean foiEquipe1Vencedora() {
		return jaOcorreu() && this.golsEquipe1.compareTo(this.golsEquipe2) > 0;
	}
	
	public boolean foiEquipe2Vencedora() {
		return jaOcorreu() && this.golsEquipe1.compareTo(this.golsEquipe2) < 0;
	}
    
    public boolean getPodeDarPalpite() {
        Calendar agora = Calendar.getInstance();
        agora.add(Calendar.HOUR_OF_DAY, 1);
        Calendar dataHoraJogo = getDataHora();
        return agora.compareTo(dataHoraJogo) < 0;
    }
	
	public boolean jaOcorreu() {
//		Calendar agora = Calendar.getInstance();
//		Calendar dataHoraJogo = getDataHora();
//		dataHoraJogo.add(Calendar.HOUR_OF_DAY, 2);
//		return agora.compareTo(dataHoraJogo) > 0;
		return true;
	}

	public boolean jaFoiAtualizado() {
		return getGolsEquipe1() != null && getGolsEquipe2() != null;
	}

	public int getFase() {
		return fase;
	}

	public void setFase(int fase) {
		this.fase = fase;
	}
    
    public String getRepresentacaoEquipes() {
        return getEquipe1().getNomePais() + " X " + getEquipe2().getNomePais();
    }

}
