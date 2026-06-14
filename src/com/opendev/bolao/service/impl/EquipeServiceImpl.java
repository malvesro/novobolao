package com.opendev.bolao.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.data.domain.Sort;

import com.opendev.bolao.model.Equipe;
import com.opendev.bolao.repository.EquipeRepository;
import com.opendev.bolao.service.EquipeService;

/**
 * Implementação do serviço de Equipe com Cache In-Memory.
 * Estratégia de Arquiteto: Reduzir roundtrips ao Aiven MySQL para dados estáticos.
 */
public class EquipeServiceImpl implements EquipeService {

	private EquipeRepository equipeRepository;
	
	// Cache simples thread-safe para equipes reais
	private List<Equipe> cachePaisesReais = null;

	public List<Equipe> buscarTodasEquipes() {
		return getEquipeRepository().findAll(Sort.by("nomePais"));
	}

	@Override
	public synchronized List<Equipe> buscarApenasPaisesReais() {
		if (this.cachePaisesReais == null) {
			List<Equipe> lista = getEquipeRepository().buscarApenasPaisesReais();
			// Criamos uma nova lista e tornamos imutável para segurança do cache
			this.cachePaisesReais = Collections.unmodifiableList(new ArrayList<>(lista));
		}
		return this.cachePaisesReais;
	}

	public EquipeRepository getEquipeRepository() {
		return equipeRepository;
	}

	public void setEquipeRepository(EquipeRepository equipeRepository) {
		this.equipeRepository = equipeRepository;
	}

}