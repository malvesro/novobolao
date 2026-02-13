package com.opendev.bolao.dao.hibernate;

import java.sql.Time;
import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.opendev.bolao.dao.JogoDao;
import com.opendev.bolao.model.Jogo;
import com.opendev.bolao.util.FiltroBuscaJogos;

public class JogoDaoImpl extends HibernateDaoSupport implements JogoDao {
	
	public Long salvar(Jogo jogo) {
		try {
			return (Long) getSession().save(jogo);
		} catch (HibernateException e) {
			throw convertHibernateAccessException(e);
		}
	}

	public List buscarTodos() {
		try {
			Query query = getSession().createQuery("from Jogo as j order by j.data asc, j.hora asc");
			return query.list();
		} catch (HibernateException e) {
			throw convertHibernateAccessException(e);
		}
	}

	public Jogo buscarPorId(Long id) {
		try {
			return (Jogo) getSession().load(Jogo.class, id);
		} catch (HibernateException e) {
			throw convertHibernateAccessException(e);
		}
	}

	public Integer buscarQuantidadeDeJogosOcorridos() {
		try {
			Query query = getSession().createQuery("select count(j.id) from Jogo as j where j.golsEquipe1 is not null and j.golsEquipe2 is not null");
			Integer qtde = (Integer) query.uniqueResult(); 
			return qtde;
		} catch (HibernateException e) {
			throw convertHibernateAccessException(e);
		}
	}

	public List buscarUsandoFiltro(FiltroBuscaJogos filtro) {
		try {
			Query query = getSession().createQuery(filtro.getHqlQuery());
			return filtro.popularParametrosDaHql(query).list();
		} catch (HibernateException e) {
			throw convertHibernateAccessException(e);
		}
	}

    public List buscarPorDataEHora(Date data, Time hora) {
        try {
            Query query = getSession().createQuery("from Jogo as j where j.data = :data and j.hora = :hora order by j.data asc, j.hora asc");
            query.setDate("data", data);
            query.setTime("hora", hora);
            return query.list();
        } catch (HibernateException e) {
            throw convertHibernateAccessException(e);
        }
    }

	public List buscarJogosOcorridos() {
		try {
			Query query = getSession().createQuery("from Jogo as j where j.golsEquipe1 is not null and j.golsEquipe2 is not null order by j.data asc, j.hora asc");
			return query.list();
		} catch (HibernateException e) {
			throw convertHibernateAccessException(e);
		}
	}

    public List buscarPorData(Date data) {
        try {
            Query query = getSession().createQuery("from Jogo as j where j.data = :data order by j.data asc, j.hora asc");
            query.setDate("data", data);
            return query.list();
        } catch (HibernateException e) {
            throw convertHibernateAccessException(e);
        }
    }

    public List buscarJogosDaEquipe(Long id, int[] fases) {
        try {
            StringBuffer hql = new StringBuffer();
            hql.append("from Jogo as j ");
            hql.append("where j.equipe1.id = :idEquipe ");
            hql.append("or j.equipe2.id = :idEquipe ");
            hql.append("order by j.grupo asc");
            Query query = getSession().createQuery(hql.toString());
            query.setLong("idEquipe", id.longValue());
            return query.list();
        } catch (HibernateException e) {
            throw convertHibernateAccessException(e);
        }
    }

}
