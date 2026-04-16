package com.opendev.bolao.service.impl;

import java.util.List;
import java.util.stream.Collectors;
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
		List<Equipe> equipes = getEquipeRepository().buscarApenasPaisesReais();
		// Filtro de segurança em nível de serviço para garantir que nenhum placeholder vaze para a UI
		return equipes.stream()
				.filter(e -> e.getGrupo() != null && e.getGrupo() >= 'A' && e.getGrupo() <= 'L')
				.filter(e -> !e.getNomePais().matches("^[0-9].*"))
				.filter(e -> !e.getNomePais().startsWith("Winner"))
				.filter(e -> !e.getNomePais().startsWith("V"))
				.collect(Collectors.toList());
	}

	public EquipeRepository getEquipeRepository() {
		return equipeRepository;
	}

	public void setEquipeRepository(EquipeRepository equipeRepository) {
		this.equipeRepository = equipeRepository;
	}

}