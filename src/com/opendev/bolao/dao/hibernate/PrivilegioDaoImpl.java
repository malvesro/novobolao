package com.opendev.bolao.dao.hibernate;

import org.hibernate.SessionFactory;

import com.opendev.bolao.dao.PriviledioDao;
import com.opendev.bolao.model.Privilegio;

public class PrivilegioDaoImpl implements PriviledioDao {

    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

	public void apagar(Privilegio p) {
        sessionFactory.getCurrentSession().delete(p);
	}

}
