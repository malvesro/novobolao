<%@include file="/WEB-INF/content/template/menu.jspf" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<div class="dashboard-section chat-page">
    <div class="chat-page__header">
        <h2><fmt:message key="chat.title" /></h2>
        <p class="chat-page__subtitle"><fmt:message key="chat.subtitle" /></p>
    </div>

    <div id="chat-feedback" class="chat-feedback" role="status" aria-live="polite">
        <c:if test="${not empty chatErro}">
            <c:out value="${chatErro}" />
        </c:if>
    </div>

    <div class="chat-layout">
        <section class="chat-stream" aria-label="<fmt:message key='chat.stream.aria' />">
            <ul id="chat-messages-list" class="chat-messages" aria-live="polite" aria-relevant="additions">
                <c:forEach var="mensagem" items="${mensagensChat}">
                    <li class="chat-message ${mensagem.autoriaDoUsuarioAtual ? 'chat-message--self' : ''}">
                        <div class="chat-message__meta">
                            <strong><c:out value="${mensagem.nomeExibicao}" /></strong>
                            <span class="chat-message__login">(@<c:out value="${mensagem.loginAutor}" />)</span>
                            <span class="chat-message__time">
                                <fmt:formatDate value="${mensagem.dataEnvio}" pattern="dd/MM/yyyy HH:mm:ss" />
                            </span>
                        </div>
                        <c:if test="${mensagem.replyToMensagemId ne null}">
                            <div class="chat-message__reply-context">
                                <p class="chat-message__reply-meta">
                                    <strong><fmt:message key="chat.reply.context.prefix" /> <c:out value="${mensagem.replyToNomeExibicao}" /></strong>
                                    <c:if test="${mensagem.replyToDataEnvio ne null}">
                                        <span class="chat-message__time">
                                            <fmt:formatDate value="${mensagem.replyToDataEnvio}" pattern="dd/MM/yyyy HH:mm:ss" />
                                        </span>
                                    </c:if>
                                </p>
                                <p class="chat-message__reply-text"><c:out value="${mensagem.replyToTextoPreview}" /></p>
                            </div>
                        </c:if>
                        <p class="chat-message__text"><c:out value="${mensagem.texto}" /></p>
                        <div class="chat-message__actions">
                            <button type="button"
                                    class="chat-message__reply-action"
                                    data-chat-reply-id="${mensagem.id}"
                                    data-chat-reply-author="${mensagem.nomeExibicao}"
                                    data-chat-reply-text="${fn:escapeXml(mensagem.texto)}">
                                <fmt:message key="chat.reply.action" />
                            </button>
                        </div>
                    </li>
                </c:forEach>
            </ul>

            <c:if test="${empty mensagensChat}">
                <p class="chat-empty"><fmt:message key="chat.empty" /></p>
            </c:if>

            <form id="chat-poll-form"
                  class="chat-hidden"
                  hx-get="${base}/seguro/chatMensagensPartial.action"
                  hx-trigger="load delay:700ms, every 5s"
                  hx-target="#chat-messages-list"
                  hx-swap="beforeend"
                  hx-include="#chat-ultimo-id">
            </form>

            <input type="hidden" id="chat-ultimo-id" name="chatUltimoId" value="${chatUltimoId}" />
        </section>

        <aside class="chat-sidebar" aria-label="<fmt:message key='chat.sidebar.aria' />">
            <h3 id="chat-participantes-titulo"><fmt:message key="chat.total" />: <c:out value="${fn:length(participantesOnlineChat)}" /></h3>
            <ul id="chat-participantes-list" class="chat-participants">
                <c:forEach var="participante" items="${participantesOnlineChat}">
                    <li><c:out value="${participante}" /></li>
                </c:forEach>
            </ul>
        </aside>
    </div>

    <form id="chat-send-form"
          class="chat-form"
          method="post"
          hx-post="${base}/seguro/chatEnviarMensagemPartial.action"
          hx-target="#chat-messages-list"
          hx-swap="beforeend"
          hx-include="#chat-ultimo-id"
          aria-busy="false">
        <div class="chat-form__row">
            <input type="hidden" id="chat-reply-message-id" name="chatReplyMensagemId" value="" />
            <div id="chat-reply-context" class="chat-reply-context" hidden aria-hidden="true">
                <p id="chat-reply-label" class="chat-reply-context__label"></p>
                <p id="chat-reply-preview" class="chat-reply-context__preview"></p>
                <button type="button" id="chat-reply-cancel" class="chat-reply-context__cancel">
                    <fmt:message key="chat.reply.cancel" />
                </button>
            </div>

            <label for="chat-mensagem"><fmt:message key="chat.message.label" /></label>
            <textarea id="chat-mensagem"
                      name="chatMensagem"
                      maxlength="300"
                      rows="3"
                      required
                      aria-describedby="chat-mention-help chat-char-counter"
                      placeholder="<fmt:message key='chat.message.placeholder' />"></textarea>
            <div class="chat-form__meta" aria-live="polite">
                <p id="chat-mention-help" class="chat-mention-hint">Use @login ou @Todos para mencionar.</p>
                <span id="chat-char-counter" class="chat-char-counter">0/300</span>
            </div>
            <div id="chat-autocomplete" class="chat-autocomplete" role="listbox" aria-label="Sugestões de menção" hidden></div>
        </div>
        <div class="chat-form__actions">
            <button type="button"
                    id="chat-new-messages-indicator"
                    class="button button--secondary chat-new-messages"
                    hidden
                    aria-hidden="true"
                    aria-controls="chat-messages-list">
                <span id="chat-new-messages-label">Novas mensagens · Ir para o final</span>
            </button>
            <button type="submit" class="button button--primary" aria-label="Enviar mensagem">
                Enviar
            </button>
        </div>
    </form>

    <section class="chat-query" aria-label="<fmt:message key='chat.query.aria' />">
        <h3><fmt:message key="chat.query.title" /></h3>
        <form id="chat-query-form"
              class="chat-query__form"
              hx-get="${base}/seguro/chatHistoricoConsultaPartial.action"
              hx-target="#chat-query-results"
              hx-swap="innerHTML">
            <input type="text" name="chatBuscaTermo" maxlength="60" placeholder="<fmt:message key='chat.query.term.placeholder' />" />
            <input type="text" name="chatBuscaAutor" maxlength="32" placeholder="<fmt:message key='chat.query.author.placeholder' />" />
            <input type="date" name="chatBuscaDataInicio" aria-label="<fmt:message key='chat.query.date.start' />" />
            <input type="date" name="chatBuscaDataFim" aria-label="<fmt:message key='chat.query.date.end' />" />
            <button type="submit" class="button button--secondary"><fmt:message key="chat.query.submit" /></button>
            <button type="button" id="chat-query-clear" class="button button--link"><fmt:message key="chat.query.clear" /></button>
        </form>
        <div id="chat-query-results" class="chat-query__results">
            <p class="chat-empty"><fmt:message key="chat.query.tip" /></p>
        </div>
    </section>

    <section class="chat-mentions-history" aria-label="<fmt:message key='chat.mentions.history.aria' />">
        <h3><fmt:message key="chat.mentions.history.title" /></h3>
        <c:if test="${empty historicoMencoes}">
            <p class="chat-empty"><fmt:message key="chat.mentions.history.empty" /></p>
        </c:if>
        <c:if test="${not empty historicoMencoes}">
            <ul class="chat-messages">
                <c:forEach var="mencao" items="${historicoMencoes}">
                    <li class="chat-message">
                        <div class="chat-message__meta">
                            <strong><c:out value="${mencao.autorNomeExibicao}" /></strong>
                            <span class="chat-message__login">(@<c:out value="${mencao.autorLogin}" />)</span>
                            <span class="chat-message__time">
                                <fmt:formatDate value="${mencao.dataCriacao}" pattern="dd/MM/yyyy HH:mm" />
                            </span>
                        </div>
                        <p class="chat-message__text"><c:out value="${mencao.mensagemPreview}" /></p>
                    </li>
                </c:forEach>
            </ul>
        </c:if>
    </section>
</div>
