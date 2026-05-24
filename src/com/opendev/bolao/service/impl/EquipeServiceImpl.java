package com.opendev.bolao.service.impl;

import java.util.List;
import org.springframework.data.domain.Sort;

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
		return getEquipeRepository().findAll(Sort.by("nomePais"));
	}

	@Override
	public List<Equipe> buscarApenasPaisesReais() {
		// O repositório já realiza a filtragem via HQL (Grupo A-L e exclusão de placeholders conhecidos)
		return getEquipeRepository().buscarApenasPaisesReais();
	}

	public EquipeRepository getEquipeRepository() {
		return equipeRepository;
	}

	public void setEquipeRepository(EquipeRepository equipeRepository) {
		this.equipeRepository = equipeRepository;
	}

}