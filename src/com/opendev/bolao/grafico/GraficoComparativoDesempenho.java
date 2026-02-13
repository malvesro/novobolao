package com.opendev.bolao.grafico;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.jfree.data.general.Dataset;

import de.laures.cewolf.DatasetProduceException;
import de.laures.cewolf.DatasetProducer;

public class GraficoComparativoDesempenho implements DatasetProducer {

	private static final long serialVersionUID = 1L;
	private static final String CID = "PERFORMANCE_CHART";
	public static final String SESSION_ATTRIBUTE = "graficoDesempenhoDS";
	
	private Dataset dadosGraficos;
    private List participantes;
	
	public GraficoComparativoDesempenho(Dataset dadosGraficos) {
		setDadosGraficos(dadosGraficos);
	}

	public Object produceDataset(Map params) throws DatasetProduceException {
		return getDadosGraficos();
	}

	public boolean hasExpired(Map params, Date since) {
		return true;
	}

	public String getProducerId() {
		return CID;
	}

	public Dataset getDadosGraficos() {
		return dadosGraficos;
	}

	public void setDadosGraficos(Dataset dadosGraficos) {
		this.dadosGraficos = dadosGraficos;
	}
    
    public List getParticipantes() {
        return this.participantes;
    }
    
    public void setParticipantes(List participantes) {
        this.participantes = participantes;
    }

//    public String generateLink(Object data, int index, Object category) {
//        return getParticipantes().;
//    }

}
