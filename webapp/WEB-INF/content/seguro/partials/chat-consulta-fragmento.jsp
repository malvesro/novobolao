<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:if test="${not empty chatErro}">
    <p class="chat-empty chat-empty--error"><c:out value="${chatErro}" /></p>
</c:if>

<c:if test="${empty mensagensConsulta and empty chatErro}">
    <p class="chat-empty"><fmt:message key="chat.query.empty" /></p>
</c:if>

<c:if test="${not empty mensagensConsulta}">
    <ul class="chat-messages chat-messages--query">
        <c:forEach var="mensagem" items="${mensagensConsulta}">
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
            </li>
        </c:forEach>
    </ul>
</c:if>
