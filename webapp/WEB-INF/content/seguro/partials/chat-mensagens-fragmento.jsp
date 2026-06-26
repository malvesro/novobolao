<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

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

<input type="hidden" id="chat-ultimo-id" name="chatUltimoId" value="${chatUltimoId}" hx-swap-oob="true" />

<div id="chat-feedback" class="chat-feedback ${not empty chatErro ? 'chat-feedback--error' : ''}" role="status" aria-live="polite" hx-swap-oob="outerHTML">
    <c:if test="${not empty chatErro}">
        <c:out value="${chatErro}" />
    </c:if>
</div>

<h3 id="chat-participantes-titulo" hx-swap-oob="outerHTML">
    <fmt:message key="chat.total" />: <c:out value="${fn:length(participantesOnlineChat)}" />
</h3>
<ul id="chat-participantes-list" class="chat-participants" hx-swap-oob="outerHTML">
    <c:forEach var="participante" items="${participantesOnlineChat}">
        <li><c:out value="${participante}" /></li>
    </c:forEach>
</ul>
