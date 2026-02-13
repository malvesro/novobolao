package com.opendev.bolao.grafico;

import java.util.Date;
import java.util.Map;

import org.jfree.data.general.Dataset;

import de.laures.cewolf.DatasetProduceException;
import de.laures.cewolf.DatasetProducer;


public class GraficoBarraLideres implements DatasetProducer {

    private static final long serialVersionUID = 1L;
    private static final String CID = "LIDERES_CHART";
    
    private Dataset dadosGraficos;
    
    public GraficoBarraLideres(Dataset dadosGraficos) {
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
    
    private Dataset getDadosGraficos() {
        return this.dadosGraficos;
    }
    
    private void setDadosGraficos(Dataset dadosGraficos) {
        this.dadosGraficos = dadosGraficos;
    }

}
