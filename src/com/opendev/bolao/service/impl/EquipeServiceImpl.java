package com.opendev.bolao.service.impl;

import java.util.List;

import com.opendev.bolao.dao.EquipeDao;
import com.opendev.bolao.service.EquipeService;

public class EquipeServiceImpl implements EquipeService {
	
	private EquipeDao equipeDao;	

	public List buscarTodasEquipes() {
		return equipeDao.buscarTodas();
	}

	public EquipeDao getEquipeDao() {
		return equipeDao;
	}

	public void setEquipeDao(EquipeDao equipeDao) {
		this.equipeDao = equipeDao;
	}

}
