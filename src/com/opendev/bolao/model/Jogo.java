package com.opendev.bolao.model;

import java.io.Serializable;
import java.sql.Time;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Locale;

import com.opendev.bolao.util.BolaoTime;
import com.opendev.bolao.util.FaseUtils;
import org.springframework.context.i18n.LocaleContextHolder;

public class Jogo implements Serializable, Comparable<Jogo> {

	private static final long serialVersionUID = 1L;
    private static final ZoneId ZONE_ID = BolaoTime.getZoneId();
    private static final int JANELA_CONCLUSAO_HORAS = 2;

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
	
	private transient ZonedDateTime dataHora;

	public Date getData() {
		return data;
	}

	public void setData(Date data) {
		this.data = data;
        this.dataHora = null;
	}

	public Time getHora() {
		return hora;
	}

	public void setHora(Time hora) {
		this.hora = hora;
        this.dataHora = null;
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

	public ZonedDateTime getDataHora() {
        if (this.dataHora == null && this.data != null && this.hora != null) {
            LocalDate localDate = Instant.ofEpochMilli(this.data.getTime()).atZone(ZONE_ID).toLocalDate();
            LocalTime localTime = this.hora.toLocalTime();
            this.dataHora = ZonedDateTime.of(localDate, localTime, ZONE_ID);
        }
		return this.dataHora;
	}

    @Override
	public int compareTo(Jogo other) {
        if (other == null) {
            return 1;
        }
        ZonedDateTime atual = this.getDataHora();
        ZonedDateTime comparacao = other.getDataHora();
        if (atual == null && comparacao == null) {
            return 0;
        }
        if (atual == null) {
            return -1;
        }
        if (comparacao == null) {
            return 1;
        }
		return atual.compareTo(comparacao);
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
        ZonedDateTime dataHoraJogo = getDataHora();
        if (dataHoraJogo == null) {
            return false;
        }
        ZonedDateTime agoraComMargem = ZonedDateTime.now(ZONE_ID).plusHours(1);
        return agoraComMargem.isBefore(dataHoraJogo);
    }
	
	public boolean jaOcorreu() {
        ZonedDateTime dataHoraJogo = getDataHora();
        if (dataHoraJogo == null) {
            return false;
        }
        ZonedDateTime corte = dataHoraJogo.plusHours(JANELA_CONCLUSAO_HORAS);
        ZonedDateTime agora = ZonedDateTime.now(ZONE_ID);
        return agora.isAfter(corte);
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

    public boolean isFaseDeGrupos() {
        return FaseUtils.isFaseDeGrupos(this.fase);
    }

    public String getDescricaoFase() {
        Locale locale = LocaleContextHolder.getLocale();
        return FaseUtils.getDescricaoFase(this.fase, locale);
    }
    
    public String getRepresentacaoEquipes() {
        return getEquipe1().getNomePais() + " X " + getEquipe2().getNomePais();
    }

}
