package com.opendev.bolao.dao.hibernate;

import org.hibernate.HibernateException;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.opendev.bolao.dao.PriviledioDao;
import com.opendev.bolao.model.Privilegio;

public class PrivilegioDaoImpl extends HibernateDaoSupport implements PriviledioDao {

	public void apagar(Privilegio p) {
	       try {
	            getSession().delete(p);
	        } catch (HibernateException e) {
	            throw convertHibernateAccessException(e);
	        }
	}

}
