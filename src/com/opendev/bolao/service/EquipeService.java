package com.opendev.bolao.service;

import java.util.List;
import com.opendev.bolao.model.Equipe;

public interface EquipeService {

	public List buscarTodasEquipes();

	/**
	 * Busca apenas as seleções nacionais (países) reais, ignorando placeholders.
	 * @return Lista de equipes reais ordenada por nome.
	 */
	public List<Equipe> buscarApenasPaisesReais();

}
