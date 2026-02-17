package com.opendev.bolao.dao.hibernate;

import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import com.opendev.bolao.dao.PalpiteDao;
import com.opendev.bolao.model.Jogo;
import com.opendev.bolao.model.Palpite;
import com.opendev.bolao.model.Participante;

public class PalpiteDaoImpl implements PalpiteDao {

    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

	public Palpite buscarPorParticipanteEJogo(Participante participante, Jogo jogo) {
		Query query = sessionFactory.getCurrentSession().createQuery("from Palpite as p where p.participante = :participante and p.jogo = :jogo");
		query.setParameter("participante", participante);
		query.setParameter("jogo", jogo);
		return (Palpite) query.uniqueResult();
	}
	
	public void salvar(Palpite palpite) {
		sessionFactory.getCurrentSession().saveOrUpdate(palpite);
	}

	public Palpite buscarPorParticipanteEJogo(String login, Long idJogo) {
		Query query = sessionFactory.getCurrentSession().createQuery("from Palpite as p where p.participante.login = :participante and p.jogo.id = :jogo");
		query.setParameter("participante", login);
		query.setParameter("jogo", idJogo.longValue());
		return (Palpite) query.uniqueResult();
	}

    public List buscarPorJogo(Long idJogo) {
        Query query = sessionFactory.getCurrentSession().createQuery("from Palpite as p where p.jogo.id = :jogo order by p.participante.login asc");
        query.setParameter("jogo", idJogo.longValue());
        return query.list();
    }

	public List buscarPorParticipante(String login) {
        Query query = sessionFactory.getCurrentSession().createQuery("from Palpite as p where p.participante.login = :login order by p.jogo.data asc, p.jogo.hora asc");
        query.setParameter("login", login);
        return query.list();
	}

}
