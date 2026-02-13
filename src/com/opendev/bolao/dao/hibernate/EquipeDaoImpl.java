package com.opendev.bolao.dao.hibernate;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.opendev.bolao.dao.EquipeDao;
import com.opendev.bolao.model.Equipe;

public class EquipeDaoImpl extends HibernateDaoSupport implements EquipeDao {

	public List buscarTodas() {
		Query query = getSession().createQuery("from Equipe as e order by e.grupo asc, e.id asc");
		List resultado = null;
		try {
			resultado = query.list();
		} catch (HibernateException e) {
			throw convertHibernateAccessException(e);
		}
		return resultado;
	}

	public Equipe buscarPorId(Long id) {
		try {
			return (Equipe) getSession().load(Equipe.class, id);
		} catch (HibernateException e) {
			throw convertHibernateAccessException(e);
		}
	}

}
