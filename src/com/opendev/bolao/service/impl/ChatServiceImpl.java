package com.opendev.bolao.service.impl;

import com.opendev.bolao.exception.BusinessException;
import com.opendev.bolao.model.ChatMensagem;
import com.opendev.bolao.model.Participante;
import com.opendev.bolao.repository.ChatMensagemRepository;
import com.opendev.bolao.repository.ParticipanteRepository;
import com.opendev.bolao.service.ChatService;
import com.opendev.bolao.service.dto.ChatMensagemView;
import com.opendev.bolao.util.SanitizationUtils;
import com.opendev.bolao.util.ValidacaoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ChatServiceImpl implements ChatService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatServiceImpl.class);
    private static final int LIMITE_MENSAGEM = 300;
    private static final int LIMITE_APELIDO = 40;
    private static final int JANELA_INICIAL = 50;
    private static final int JANELA_INCREMENTAL = 50;
    private static final int LIMITE_ENVIO_POR_JANELA = 10;
    private static final long JANELA_ENVIO_MS = 10_000L;
    private static final int LIMITE_POLL_POR_JANELA = 30;
    private static final long JANELA_POLL_MS = 10_000L;
    private static final long TTL_PRESENCA_MS = 120_000L;

    private ChatMensagemRepository chatMensagemRepository;
    private ParticipanteRepository participanteRepository;

    private final ConcurrentMap<String, String> apelidosPorSessao = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Long> ultimaAtividadeApelidoPorSessao = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Long> ultimaAtividadePorLogin = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Deque<Long>> trilhasEnvioPorLogin = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Deque<Long>> trilhasPollingPorLogin = new ConcurrentHashMap<>();

    @Override
    public List<ChatMensagemView> buscarMensagensIniciais(String loginAtual) {
        atualizarPresenca(loginAtual);
        List<ChatMensagem> mensagens = chatMensagemRepository.findAllByOrderByIdDesc(PageRequest.of(0, JANELA_INICIAL));
        List<ChatMensagem> ordemCrescente = new ArrayList<>(mensagens);
        ordemCrescente.sort(Comparator.comparing(ChatMensagem::getId));
        return mapearParaView(ordemCrescente, loginAtual);
    }

    @Override
    public List<ChatMensagemView> buscarMensagensIncrementais(String loginAtual, Long ultimoIdRecebido) {
        String loginSeguro = sanitizarLoginObrigatorio(loginAtual);
        atualizarPresenca(loginSeguro);
        aplicarRateLimitPolling(loginSeguro);
        Long ultimoIdSeguro = (ultimoIdRecebido == null || ultimoIdRecebido < 0) ? 0L : ultimoIdRecebido;
        List<ChatMensagem> mensagens = chatMensagemRepository.findByIdGreaterThanOrderByIdAsc(
                ultimoIdSeguro, PageRequest.of(0, JANELA_INCREMENTAL));
        return mapearParaView(mensagens, loginSeguro);
    }

    @Override
    public List<String> buscarParticipantesOnline() {
        limparEstruturasEmMemoria();
        long limite = System.currentTimeMillis() - TTL_PRESENCA_MS;
        List<String> participantes = new ArrayList<>();

        for (Map.Entry<String, Long> entry : ultimaAtividadePorLogin.entrySet()) {
            String login = entry.getKey();
            Long ultimoAcesso = entry.getValue();
            if (ultimoAcesso == null || ultimoAcesso < limite) {
                continue;
            }
            participantes.add(resolverNomeExibicao(login, null));
        }

        participantes.sort(String.CASE_INSENSITIVE_ORDER);
        return participantes;
    }

    @Override
    public ChatMensagemView criarMensagem(String loginAtual, String chaveSessao, String apelido, String texto, String ipOrigem) {
        String loginSeguro = sanitizarLoginObrigatorio(loginAtual);
        String chaveSessaoSegura = sanitizarChaveSessao(chaveSessao, loginSeguro);
        atualizarPresenca(loginSeguro);
        aplicarRateLimitEnvio(loginSeguro);

        if (!ValidacaoUtils.isVazia(apelido)) {
            String apelidoSeguro = sanitizarApelido(apelido);
            if (!ValidacaoUtils.isVazia(apelidoSeguro)) {
                apelidosPorSessao.put(chaveSessaoSegura, apelidoSeguro);
                ultimaAtividadeApelidoPorSessao.put(chaveSessaoSegura, System.currentTimeMillis());
            }
        }

        String textoSeguro = sanitizarMensagem(texto);
        ChatMensagem novaMensagem = new ChatMensagem();
        novaMensagem.setLoginAutor(loginSeguro);
        novaMensagem.setNomeExibicao(resolverNomeExibicao(loginSeguro, chaveSessaoSegura));
        novaMensagem.setTexto(textoSeguro);
        novaMensagem.setDataEnvio(new Date());

        ChatMensagem salva = chatMensagemRepository.save(novaMensagem);
        LOGGER.info("[CHAT][SEND] user={} ip={} messageId={} status=SUCCESS", loginSeguro, ipOrigem, salva.getId());
        return mapearParaView(salva, loginSeguro);
    }

    @Override
    public void atualizarPresenca(String loginAtual) {
        if (ValidacaoUtils.isVazia(loginAtual)) {
            return;
        }
        ultimaAtividadePorLogin.put(loginAtual.trim().toLowerCase(Locale.ROOT), System.currentTimeMillis());
    }

    private String sanitizarLoginObrigatorio(String loginAtual) {
        String login = loginAtual == null ? null : loginAtual.trim().toLowerCase(Locale.ROOT);
        if (ValidacaoUtils.isVazia(login)) {
            throw new BusinessException(BusinessException.Code.INVALID_INPUT, "Sessão inválida para uso do chat.");
        }
        return login;
    }

    private String sanitizarChaveSessao(String chaveSessao, String loginFallback) {
        if (!ValidacaoUtils.isVazia(chaveSessao)) {
            return chaveSessao.trim();
        }
        return "login:" + loginFallback;
    }

    private String sanitizarMensagem(String texto) {
        String valor = texto == null ? "" : texto;
        if (SanitizationUtils.containsHtml(valor)) {
            throw new BusinessException(BusinessException.Code.INVALID_INPUT, "Mensagem inválida: HTML não é permitido.");
        }
        String limpo = SanitizationUtils.cleanText(valor, LIMITE_MENSAGEM);
        if (ValidacaoUtils.isVazia(limpo)) {
            throw new BusinessException(BusinessException.Code.INVALID_INPUT, "Digite uma mensagem para enviar.");
        }
        if (limpo.length() > LIMITE_MENSAGEM) {
            throw new BusinessException(BusinessException.Code.INVALID_INPUT, "Mensagem excede o limite permitido.");
        }
        return limpo;
    }

    private String sanitizarApelido(String apelido) {
        String valor = apelido == null ? "" : apelido;
        if (SanitizationUtils.containsHtml(valor)) {
            throw new BusinessException(BusinessException.Code.INVALID_INPUT, "Apelido inválido.");
        }
        String limpo = SanitizationUtils.cleanText(valor, LIMITE_APELIDO);
        if (limpo != null && limpo.length() > LIMITE_APELIDO) {
            limpo = limpo.substring(0, LIMITE_APELIDO);
        }
        return limpo;
    }

    private void aplicarRateLimitEnvio(String loginSeguro) {
        Deque<Long> trilha = trilhasEnvioPorLogin.computeIfAbsent(loginSeguro, k -> new ArrayDeque<>());
        long agora = System.currentTimeMillis();
        long limiteInferior = agora - JANELA_ENVIO_MS;

        synchronized (trilha) {
            while (!trilha.isEmpty() && trilha.peekFirst() < limiteInferior) {
                trilha.pollFirst();
            }
            if (trilha.size() >= LIMITE_ENVIO_POR_JANELA) {
                LOGGER.warn("[CHAT][RATE_LIMIT] user={} status=BLOCKED", loginSeguro);
                throw new BusinessException(BusinessException.Code.CONFLICT,
                        "Limite de mensagens atingido. Aguarde alguns segundos.");
            }
            trilha.addLast(agora);
        }
    }

    private void aplicarRateLimitPolling(String loginSeguro) {
        Deque<Long> trilha = trilhasPollingPorLogin.computeIfAbsent(loginSeguro, k -> new ArrayDeque<>());
        long agora = System.currentTimeMillis();
        long limiteInferior = agora - JANELA_POLL_MS;

        synchronized (trilha) {
            while (!trilha.isEmpty() && trilha.peekFirst() < limiteInferior) {
                trilha.pollFirst();
            }
            if (trilha.size() >= LIMITE_POLL_POR_JANELA) {
                LOGGER.warn("[CHAT][POLL_RATE_LIMIT] user={} status=BLOCKED", loginSeguro);
                throw new BusinessException(BusinessException.Code.CONFLICT,
                        "Muitas atualizações em sequência. Aguarde alguns segundos.");
            }
            trilha.addLast(agora);
        }
    }

    private String resolverNomeExibicao(String loginSeguro, String chaveSessao) {
        if (!ValidacaoUtils.isVazia(chaveSessao)) {
            String apelidoMemoria = apelidosPorSessao.get(chaveSessao);
            if (!ValidacaoUtils.isVazia(apelidoMemoria)) {
                ultimaAtividadeApelidoPorSessao.put(chaveSessao, System.currentTimeMillis());
                return apelidoMemoria;
            }
        }

        Optional<Participante> participante = participanteRepository.findByLogin(loginSeguro);
        if (participante.isPresent() && !ValidacaoUtils.isVazia(participante.get().getNome())) {
            return participante.get().getNome().trim();
        }

        return loginSeguro;
    }

    private void limparEstruturasEmMemoria() {
        long limite = System.currentTimeMillis() - TTL_PRESENCA_MS;
        ultimaAtividadePorLogin.entrySet().removeIf(entry -> entry.getValue() == null || entry.getValue() < limite);
        ultimaAtividadeApelidoPorSessao.entrySet()
                .removeIf(entry -> entry.getValue() == null || entry.getValue() < limite);
        apelidosPorSessao.keySet().removeIf(chave -> !ultimaAtividadeApelidoPorSessao.containsKey(chave));
    }

    private List<ChatMensagemView> mapearParaView(List<ChatMensagem> mensagens, String loginAtual) {
        if (mensagens == null || mensagens.isEmpty()) {
            return Collections.emptyList();
        }
        List<ChatMensagemView> views = new ArrayList<>(mensagens.size());
        for (ChatMensagem mensagem : mensagens) {
            views.add(mapearParaView(mensagem, loginAtual));
        }
        return views;
    }

    private ChatMensagemView mapearParaView(ChatMensagem mensagem, String loginAtual) {
        String loginMensagem = mensagem.getLoginAutor() == null ? "" : mensagem.getLoginAutor();
        String loginAtualSeguro = loginAtual == null ? "" : loginAtual;
        boolean autoriaAtual = Objects.equals(loginMensagem, loginAtualSeguro);
        return new ChatMensagemView(
                mensagem.getId(),
                loginMensagem,
                mensagem.getNomeExibicao(),
                mensagem.getTexto(),
                mensagem.getDataEnvio(),
                autoriaAtual);
    }

    public void setChatMensagemRepository(ChatMensagemRepository chatMensagemRepository) {
        this.chatMensagemRepository = chatMensagemRepository;
    }

    public void setParticipanteRepository(ParticipanteRepository participanteRepository) {
        this.participanteRepository = participanteRepository;
    }
}
