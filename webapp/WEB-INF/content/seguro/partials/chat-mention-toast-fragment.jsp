<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:url var="chatMentionsNotificationUrl" value="/seguro/chatMencoesNotification.action" />
<c:url var="chatMentionsAckUrl" value="/seguro/chatMencoesAckPartial.action" />
<c:url var="chatPageUrl" value="/seguro/batePapo.action" />
<c:if test="${not empty notificacoesMencao}">
<c:set var="chatMencoesAckIdsCsv" value="" />
<c:forEach var="notificacaoId" items="${notificacoesMencao}">
    <c:if test="${not empty notificacaoId.chatMensagemId}">
        <c:set var="chatMencoesAckIdsCsv" value="${chatMencoesAckIdsCsv}${empty chatMencoesAckIdsCsv ? '' : ','}${notificacaoId.chatMensagemId}" />
    </c:if>
</c:forEach>
<div id="chat-mentions-poller"
     hx-get="${chatMentionsNotificationUrl}"
     hx-trigger="mentions:refresh, every 15s"
     hx-swap="outerHTML"
     data-chat-mentions-ack-url="${chatMentionsAckUrl}"
     data-chat-mentions-ack-ids="${chatMencoesAckIdsCsv}"
     class="chat-mentions-poller">
    <div class="mention-toast-container" role="status" aria-live="polite">
        <c:forEach var="notificacao" items="${notificacoesMencao}">
            <c:set var="autorNomeExibicaoEscapado" value="${fn:escapeXml(notificacao.autorNomeExibicao)}" />
            <div class="mention-toast">
                <p class="mention-toast__message">
                    <fmt:message key="chat.notification.mention">
                        <fmt:param value="${autorNomeExibicaoEscapado}" />
                    </fmt:message>
                </p>
                <p class="mention-toast__preview"><c:out value="${notificacao.mensagemPreview}" /></p>
                <a class="mention-toast__link" href="${chatPageUrl}">
                    <fmt:message key="chat.notification.seeChat" />
                </a>
            </div>
        </c:forEach>
    </div>
</div>
</c:if>
