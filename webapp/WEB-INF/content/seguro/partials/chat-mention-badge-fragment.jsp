<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:url var="chatMentionsBadgeUrl" value="/seguro/chatMencoesBadgePartial.action" />
<c:if test="${chatMencoesPendentes gt 0}">
<div id="chat-mentions-badge-poller"
     class="chat-mentions-badge-poller"
     hx-get="${chatMentionsBadgeUrl}"
     hx-trigger="mentions:refresh, every 15s"
     hx-swap="outerHTML">
    <span class="badge badge--pending" title="<fmt:message key='chat.notification.badge.title' />">
        <fmt:message key="chat.notification.badge">
            <fmt:param value="${chatMencoesPendentes}" />
        </fmt:message>
    </span>
</div>
</c:if>
