package com.opendev.bolao.chat;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.opendev.bolao.service.ParticipanteService;
import com.opendev.bolao.util.BolaoTime;
import com.opendev.bolao.util.RequestUtils;
import com.opendev.bolao.util.ValidacaoUtils;

public class BatePapo implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final String AUTO_MSG = "auto";

	private FormatadorMensagem formatadorMensagem;
    private ParticipanteService participanteService;
	
	private Long proximoId = null;
	private Map mensagens = null;
	private Map apelidos = null;
    private List participantes = null;
    private DateFormat formatadorData = null;
    private long tempoUltimaMsg = 0L;
	
	public BatePapo() {
		this.proximoId = new Long(0);
        this.mensagens = Collections.synchronizedMap(new LinkedHashMap());
        this.apelidos = new HashMap();
        this.participantes = Collections.synchronizedList(new ArrayList(28));
        this.formatadorData = new SimpleDateFormat("HH:mm:ss");
        // Carimbo de horário do chat alinhado ao fuso oficial do domínio.
        this.formatadorData.setTimeZone(BolaoTime.getTimeZone());
        this.tempoUltimaMsg = System.currentTimeMillis();
	}

    public void entrar() {
        synchronized (this.participantes) {
            String login = RequestUtils.getLoginParticipanteAutenticado();
            if (login != null && !this.participantes.contains(login)) {
                this.participantes.add(login);
                enviarMensagem("[i]" + login + "[/i] entrou na sala.", AUTO_MSG);
            }
        }
    }
    
    public void sair() {
        synchronized (this.participantes) {
            String login = RequestUtils.getLoginParticipanteAutenticado();
            if (login != null && !this.participantes.contains(login)) {
                this.participantes.remove(login);
                enviarMensagem("[i]" + login + "[/i] saiu na sala.", AUTO_MSG);
            }
        }
    }

	public Mensagem enviarMensagem(String msg) {
		return enviarMensagem(msg, RequestUtils.getLoginParticipanteAutenticado());
	}
	
	private Mensagem enviarMensagem(String msg, String login) {
		Mensagem mensagem = null;
		synchronized (this.proximoId) {
			mensagem = new Mensagem();
			this.proximoId = new Long(this.proximoId.longValue() + 1);
			mensagem.setId(this.proximoId);
			mensagem.setLoginParticipante(login);
			mensagem.setApelidoParticipante((String) this.apelidos.get(login));
			mensagem.setDataHoraEnvio(this.formatadorData.format(new Date()));
			mensagem.setTexto(msg);
			getFormatadorMensagem().formatar(mensagem);
			synchronized (this.mensagens) {
			    this.mensagens.put(this.proximoId, mensagem);
			    tempoUltimaMsg = System.currentTimeMillis();
			}
			
		}
		return mensagem;
	}
    
    public List buscarMensagens(Long id) {
        List mensagens = new ArrayList(this.mensagens.values());
        Collections.sort(mensagens);
        if (id.longValue() != -1L) {
            Mensagem referencia = (Mensagem) this.mensagens.get(id);
            int index = Collections.binarySearch(mensagens, referencia);
            if (index >= (mensagens.size() - 1)) {
                mensagens = Collections.EMPTY_LIST;
            } else {
                mensagens = mensagens.subList(index + 1, mensagens.size());
            }
        }
        return mensagens;
    }
    
    public List buscarTodosParticipantes() {
        List todos = new ArrayList(this.participantes.size());
        String participante = null;
        String apelido = null;
        synchronized (this.participantes) {
            for (Iterator iter = this.participantes.iterator(); iter.hasNext();) {
                participante = (String) iter.next();
                apelido = (String) this.apelidos.get(participante);
                if (!ValidacaoUtils.isVazia(apelido)) {
                    participante = apelido + " (" + participante + ")";
                }
                todos.add(participante);
            }
            Collections.sort(todos, String.CASE_INSENSITIVE_ORDER);
        }
//        if ((System.currentTimeMillis() - tempoUltimaMsg) >= 120000L) {
//        	synchronized (this.mensagens) {
//        		this.mensagens.clear();
//			}
//        }
        return todos;
    }
    
    public Long buscarIdUltimaMensagem() {
    	return this.proximoId;
    }

	public void buscarInformacoesDoParticipante(String login) {
		// TODO Auto-generated method stub
		
	}
    
    public void alterarApelido(String apelido) {
        if (ValidacaoUtils.isVazia(apelido)) {
            return;
        }
        String login = RequestUtils.getLoginParticipanteAutenticado();
        synchronized (this.apelidos) {
            this.apelidos.put(login, apelido);
        }
    }

    public FormatadorMensagem getFormatadorMensagem() {
        return formatadorMensagem;
    }

    public void setFormatadorMensagem(FormatadorMensagem formatadorMensagem) {
        this.formatadorMensagem = formatadorMensagem;
    }

    public ParticipanteService getParticipanteService() {
        return this.participanteService;
    }

    public void setParticipanteService(ParticipanteService participanteService) {
        this.participanteService = participanteService;
    }
	
}
