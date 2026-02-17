package com.opendev.bolao.dao.hibernate;

import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import com.opendev.bolao.dao.EquipeDao;
import com.opendev.bolao.model.Equipe;

public class EquipeDaoImpl implements EquipeDao {

    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

	public List buscarTodas() {
		Query query = sessionFactory.getCurrentSession().createQuery("from Equipe as e order by e.grupo asc, e.id asc");
		return query.list();
	}

	public Equipe buscarPorId(Long id) {
		return (Equipe) sessionFactory.getCurrentSession().load(Equipe.class, id);
	}

}
