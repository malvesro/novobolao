package com.opendev.bolao.dao;

import java.util.List;

import com.opendev.bolao.model.Participante;

public interface ParticipanteDao {

	public Participante buscarPorLogin(String login);

	public List buscarTodosDoBolaoGeral();

    public List buscarTodos();

    public Participante buscarPorId(Long id);

    public void salvar(Participante participante);

    public void apagar(Long id);

}
