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

import jakarta.persistence.*;

/**
 * Representa um jogo da Copa do Mundo.
 * Mapeado para a tabela JOG_JOGO via Spring Data JPA.
 */
@Entity
@Table(name = "JOG_JOGO")
public class Jogo implements Serializable, Comparable<Jogo> {

	private static final long serialVersionUID = 1L;
    private static final ZoneId ZONE_ID = BolaoTime.getZoneId();

	public static final int FASE_FINAL = 1;
	public static final int FASE_TERCEIRO_LUGAR = 3;
	public static final int FASE_SEMIFINAL = 2;
	public static final int FASE_QUARTAS = 4;
	public static final int FASE_OITAVAS = 8;
	public static final int FASE_TRINTA_DOIS_AVOS = 16;
	public static final int FASE_GRUPO_RODADA_1 = 11;
	public static final int FASE_GRUPO_RODADA_2 = 12;
	public static final int FASE_GRUPO_RODADA_3 = 13;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "JOG_ID")
	private Long id;

	@Temporal(TemporalType.DATE)
	@Column(name = "JOG_DATA", nullable = false)
	private Date data;

	@Column(name = "JOG_HORA", nullable = false)
	private Time hora;

	@Column(name = "JOG_LOCAL", nullable = false, length = 100)
	private String local;

	@Column(name = "JOG_EQP1_GOLS")
	private Integer golsEquipe1;

	@Column(name = "JOG_EQP2_GOLS")
	private Integer golsEquipe2;

	@Column(name = "JOG_FASE", nullable = false)
	private int fase;

    @Column(name = "JOG_EXTERNAL_ID", length = 64, unique = true)
    private String externalId;

    @Column(name = "JOG_LAST_CHECKED")
    private java.time.Instant lastCheckedAt;

    @Column(name = "JOG_SOURCE_UPDATED")
    private java.time.Instant sourceUpdatedAt;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "JOG_EQP1_ID")
	private Equipe equipe1;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "JOG_EQP2_ID")
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
            LocalDate localDate = extrairDataLocal(this.data);
            LocalTime localTime = extrairHoraLocal(this.hora);
            this.dataHora = ZonedDateTime.of(localDate, localTime, ZONE_ID);
        }
		return this.dataHora;
	}

    private LocalDate extrairDataLocal(Date dataBase) {
        if (dataBase instanceof java.sql.Date) {
            return ((java.sql.Date) dataBase).toLocalDate();
        }
        return Instant.ofEpochMilli(dataBase.getTime()).atZone(ZONE_ID).toLocalDate();
    }

    private LocalTime extrairHoraLocal(Time horaBase) {
        LocalTime horaViaToLocalTime = horaBase.toLocalTime();
        LocalTime horaViaEpoch = Instant.ofEpochMilli(horaBase.getTime()).atZone(ZONE_ID).toLocalTime();
        if (!horaViaEpoch.equals(horaViaToLocalTime)) {
            return horaViaEpoch;
        }
        return horaViaToLocalTime;
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
		return jaOcorreu() && jaFoiAtualizado() && this.golsEquipe1.compareTo(this.golsEquipe2) == 0;
	}
	
	public boolean foiEquipe1Vencedora() {
		return jaOcorreu() && jaFoiAtualizado() && this.golsEquipe1.compareTo(this.golsEquipe2) > 0;
	}
	
	public boolean foiEquipe2Vencedora() {
		return jaOcorreu() && jaFoiAtualizado() && this.golsEquipe1.compareTo(this.golsEquipe2) < 0;
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
        ZonedDateTime agora = ZonedDateTime.now(ZONE_ID);
        return !agora.isBefore(dataHoraJogo);
	}

    public boolean jaFoiAtualizado() {
        return getGolsEquipe1() != null && getGolsEquipe2() != null;
    }

    /**
     * Sinaliza se o placar já pode ser atualizado pela área administrativa.
     * Regra: permitido a partir do início do jogo (inclui correções retroativas).
     */
    public boolean getPodeAtualizarResultado() {
        return jaOcorreu();
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


    public String getEquipe1BandeiraUrl() {
        return getEquipe1() != null ? getEquipe1().getBandeiraUrl() : "";
    }

    public String getEquipe2BandeiraUrl() {
        return getEquipe2() != null ? getEquipe2().getBandeiraUrl() : "";
    }

    public String getEquipe1SiglaPais() {
        return getEquipe1() != null ? getEquipe1().getSiglaPais() : "";
    }

    public String getEquipe1EmojiBandeira() {
        return getEquipe1() != null ? getEquipe1().getEmojiBandeira() : "";
    }

    public String getEquipe2EmojiBandeira() {
        return getEquipe2() != null ? getEquipe2().getEmojiBandeira() : "";
    }

    public String getRowStyleClass() {
        if (getEquipe1() != null && "Brasil".equals(getEquipe1().getNomePais())) return "brasil";
        if (getEquipe2() != null && "Brasil".equals(getEquipe2().getNomePais())) return "brasil";
        return ""; // Row zebra coloring is handled by CSS nth-child or rowIndex usually, but here we can return a base class
    }

    public String getEquipe2SiglaPais() {
        return getEquipe2() != null ? getEquipe2().getSiglaPais() : "";
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public java.time.Instant getLastCheckedAt() {
        return lastCheckedAt;
    }

    public void setLastCheckedAt(java.time.Instant lastCheckedAt) {
        this.lastCheckedAt = lastCheckedAt;
    }

    public java.time.Instant getSourceUpdatedAt() {
        return sourceUpdatedAt;
    }

    public void setSourceUpdatedAt(java.time.Instant sourceUpdatedAt) {
        this.sourceUpdatedAt = sourceUpdatedAt;
    }
}
