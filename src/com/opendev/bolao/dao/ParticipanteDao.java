package com.opendev.bolao.dao;

import java.util.List;
import java.util.Optional;

import com.opendev.bolao.model.Participante;

public interface ParticipanteDao {

	public Optional<Participante> buscarPorLogin(String login);
	public Optional<Participante> buscarPorEmail(String email);

	public List buscarTodosDoBolaoGeral();

    public List buscarTodos();

    public Optional<Participante> buscarPorId(Long id);

    public void salvar(Participante participante);

    public void apagar(Long id);

}
