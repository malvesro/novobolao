package com.opendev.bolao.dao.hibernate;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.opendev.bolao.dao.ParticipanteDao;
import com.opendev.bolao.model.Participante;

public class ParticipanteDaoImpl extends HibernateDaoSupport implements ParticipanteDao {

	public Participante buscarPorLogin(String login) {
		try {
			Query query = getSession().createQuery("from Participante as p where p.login = :login");
			query.setString("login", login);
			return (Participante) query.uniqueResult();
		} catch (HibernateException e) {
			throw convertHibernateAccessException(e);
		}
	}

	public List buscarTodosDoBolaoGeral() {
		try {
			Query query = getSession().createQuery("select p from Participante as p inner join p.privilegios as pri where pri.papel = :papel");
			query.setString("papel", "geral");
			return query.list();
		} catch (HibernateException e) {
			throw convertHibernateAccessException(e);
		}
	}

    public List buscarTodos() {
        try {
            Query query = getSession().createQuery("from Participante as p order by p.nome asc");
            return query.list();
        } catch (HibernateException e) {
            throw convertHibernateAccessException(e);
        }
    }

    public Participante buscarPorId(Long id) {
        try {
            return (Participante) getSession().load(Participante.class, id);
        } catch (HibernateException e) {
            throw convertHibernateAccessException(e);
        }
    }

    public void salvar(Participante participante) {
        try {
            getSession().save(participante);
        } catch (HibernateException e) {
            throw convertHibernateAccessException(e);
        }
    }

    public void apagar(Long id) {
        try {
            getSession().delete(buscarPorId(id));
        } catch (HibernateException e) {
            throw convertHibernateAccessException(e);
        }
    }

}
