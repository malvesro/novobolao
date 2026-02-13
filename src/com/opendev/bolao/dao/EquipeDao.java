package com.opendev.bolao.dao;

import java.util.List;

import com.opendev.bolao.model.Equipe;

public interface EquipeDao {
	
	public List buscarTodas();
	
	public Equipe buscarPorId(Long id);

}
