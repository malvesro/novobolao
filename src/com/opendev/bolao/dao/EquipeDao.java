package com.opendev.bolao.dao;

import java.util.List;
import java.util.Optional;

import com.opendev.bolao.model.Equipe;

public interface EquipeDao {
	
	public List buscarTodas();
	
	public Optional<Equipe> buscarPorId(Long id);

}
