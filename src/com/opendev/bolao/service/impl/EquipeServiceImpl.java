package com.opendev.bolao.service.impl;

import java.util.List;

import com.opendev.bolao.model.Equipe;
import com.opendev.bolao.repository.EquipeRepository;
import com.opendev.bolao.service.EquipeService;

/**
 * Implementação do serviço de Equipe.
 * Refatorado para utilizar Spring Data JPA Repositories.
 */
public class EquipeServiceImpl implements EquipeService {
	
	private EquipeRepository equipeRepository;	

	public List<Equipe> buscarTodasEquipes() {
		return getEquipeRepository().findAll();
	}

	public EquipeRepository getEquipeRepository() {
		return equipeRepository;
	}

	public void setEquipeRepository(EquipeRepository equipeRepository) {
		this.equipeRepository = equipeRepository;
	}

}
