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
                                <fmt:formatDate value="${mensagem.dataEnvio}" pattern="HH:mm:ss" />
                            </span>
                        </div>
                        <p class="chat-message__text"><c:out value="${mensagem.texto}" /></p>
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
          hx-include="#chat-ultimo-id, #chat-apelido"
          hx-on::after-request="if(event.detail.successful){this.querySelector('textarea[name=chatMensagem]').value='';}">
        <div class="chat-form__row">
            <label for="chat-apelido"><fmt:message key="chat.apelido" /></label>
            <input type="text" id="chat-apelido" name="chatApelido" maxlength="40" />
        </div>
        <div class="chat-form__row">
            <label for="chat-mensagem"><fmt:message key="chat.message.label" /></label>
            <textarea id="chat-mensagem"
                      name="chatMensagem"
                      maxlength="300"
                      rows="3"
                      required
                      placeholder="<fmt:message key='chat.message.placeholder' />"></textarea>
        </div>
        <div class="chat-form__actions">
            <button type="submit" class="button button--primary">
                <fmt:message key="chat.send" />
            </button>
        </div>
    </form>
</div>
