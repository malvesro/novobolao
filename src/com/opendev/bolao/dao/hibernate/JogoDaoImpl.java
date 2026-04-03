package com.opendev.bolao.dao.hibernate;

import java.sql.Time;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import com.opendev.bolao.dao.JogoDao;
import com.opendev.bolao.model.Jogo;
import com.opendev.bolao.util.FiltroBuscaJogos;

public class JogoDaoImpl implements JogoDao {
	
    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

	public Long salvar(Jogo jogo) {
		return (Long) sessionFactory.getCurrentSession().save(jogo);
	}

	public List buscarTodos() {
		Query query = sessionFactory.getCurrentSession().createQuery("from Jogo as j order by j.data asc, j.hora asc");
		return query.list();
	}

	public Optional<Jogo> buscarPorId(Long id) {
		return Optional.ofNullable((Jogo) sessionFactory.getCurrentSession().get(Jogo.class, id));
	}

	public long buscarQuantidadeDeJogosOcorridos() {
		Query<Long> query = sessionFactory.getCurrentSession()
            .createQuery("select count(j.id) from Jogo as j where j.golsEquipe1 is not null and j.golsEquipe2 is not null", Long.class);
		return query.uniqueResultOptional().orElse(0L);
	}

	public List buscarUsandoFiltro(FiltroBuscaJogos filtro) {
		Query query = sessionFactory.getCurrentSession().createQuery(filtro.getHqlQuery());
		return filtro.popularParametrosDaHql(query).list();
	}

    public List buscarPorDataEHora(Date data, Time hora) {
        Query query = sessionFactory.getCurrentSession().createQuery("from Jogo as j where j.data = :data and j.hora = :hora order by j.data asc, j.hora asc");
        query.setParameter("data", data);
        query.setParameter("hora", hora);
        return query.list();
    }

	public List buscarJogosOcorridos() {
		Query query = sessionFactory.getCurrentSession().createQuery("from Jogo as j where j.golsEquipe1 is not null and j.golsEquipe2 is not null order by j.data asc, j.hora asc");
		return query.list();
	}

    public List buscarPorData(Date data) {
        Query query = sessionFactory.getCurrentSession().createQuery("from Jogo as j where j.data = :data order by j.data asc, j.hora asc");
        query.setParameter("data", data);
        return query.list();
    }

    public List buscarJogosDaEquipe(Long id, int[] fases) {
        StringBuffer hql = new StringBuffer();
        hql.append("from Jogo as j ");
        hql.append("where j.equipe1.id = :idEquipe ");
        hql.append("or j.equipe2.id = :idEquipe ");
        hql.append("order by j.grupo asc");
        Query query = sessionFactory.getCurrentSession().createQuery(hql.toString());
        query.setParameter("idEquipe", id.longValue());
        return query.list();
    }

}
