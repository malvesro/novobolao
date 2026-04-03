package com.opendev.bolao.dao.hibernate;

import java.util.List;
import java.util.Optional;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import com.opendev.bolao.dao.ParticipanteDao;
import com.opendev.bolao.model.Participante;

public class ParticipanteDaoImpl implements ParticipanteDao {

    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

	public Optional<Participante> buscarPorLogin(String login) {
		Query query = sessionFactory.getCurrentSession().createQuery("from Participante as p where p.login = :login");
		query.setParameter("login", login);
		return Optional.ofNullable((Participante) query.uniqueResult());
	}

	public Optional<Participante> buscarPorEmail(String email) {
		Query query = sessionFactory.getCurrentSession().createQuery("from Participante as p where p.email = :email");
		query.setParameter("email", email);
		return Optional.ofNullable((Participante) query.uniqueResult());
	}

	public List buscarTodosDoBolaoGeral() {
		Query query = sessionFactory.getCurrentSession().createQuery("select p from Participante as p inner join p.privilegios as pri where pri.papel = :papel");
		query.setParameter("papel", "ROLE_USER");
		return query.list();
	}

    public List buscarTodos() {
        Query query = sessionFactory.getCurrentSession().createQuery("from Participante as p order by p.nome asc");
        return query.list();
    }

    public Optional<Participante> buscarPorId(Long id) {
        return Optional.ofNullable((Participante) sessionFactory.getCurrentSession().get(Participante.class, id));
    }

    public void salvar(Participante participante) {
        sessionFactory.getCurrentSession().save(participante);
    }

    public void apagar(Long id) {
        buscarPorId(id).ifPresent(p -> sessionFactory.getCurrentSession().delete(p));
    }

}
