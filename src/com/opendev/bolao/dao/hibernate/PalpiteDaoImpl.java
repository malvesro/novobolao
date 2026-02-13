package com.opendev.bolao.dao.hibernate;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.opendev.bolao.dao.PalpiteDao;
import com.opendev.bolao.model.Jogo;
import com.opendev.bolao.model.Palpite;
import com.opendev.bolao.model.Participante;

public class PalpiteDaoImpl extends HibernateDaoSupport implements PalpiteDao {

	public Palpite buscarPorParticipanteEJogo(Participante participante, Jogo jogo) {
		try {
			Query query = getSession().createQuery("from Palpite as p where p.participante = :participante and p.jogo = :jogo");
			query.setEntity("participante", participante);
			query.setEntity("jogo", jogo);
			return (Palpite) query.uniqueResult();
		} catch (HibernateException e) {
			throw convertHibernateAccessException(e);
		}
	}
	
	public void salvar(Palpite palpite) {
		try {
			getSession().saveOrUpdate(palpite);
		} catch (HibernateException e) {
			throw convertHibernateAccessException(e);
		}
	}

	public Palpite buscarPorParticipanteEJogo(String login, Long idJogo) {
		try {
			Query query = getSession().createQuery("from Palpite as p where p.participante.login = :participante and p.jogo.id = :jogo");
			query.setString("participante", login);
			query.setLong("jogo", idJogo.longValue());
			return (Palpite) query.uniqueResult();
		} catch (HibernateException e) {
			throw convertHibernateAccessException(e);
		}
	}

    public List buscarPorJogo(Long idJogo) {
        try {
            Query query = getSession().createQuery("from Palpite as p where p.jogo.id = :jogo order by p.participante.login asc");
            query.setLong("jogo", idJogo.longValue());
            return query.list();
        } catch (HibernateException e) {
            throw convertHibernateAccessException(e);
        }
    }

	public List buscarPorParticipante(String login) {
        try {
            Query query = getSession().createQuery("from Palpite as p where p.participante.login = :login order by p.jogo.data asc, p.jogo.hora asc");
            query.setString("login", login);
            return query.list();
        } catch (HibernateException e) {
            throw convertHibernateAccessException(e);
        }
	}

}
