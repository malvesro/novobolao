<c:set var="usuarioAutenticado" value="${not empty pageContext.request.userPrincipal}" />
<c:if test="${usuarioAutenticado}">
    <%@include file="/WEB-INF/content/template/menu.jspf" %>
</c:if>

<div class="spacer-lg"></div>
<fmt:message key="rules.title" var="rulesTitle" />

<div class="dashboard-section">
    <opendev:portlet id="regras_portlet" icon="/img/information.gif" title="${rulesTitle}">
        <div class="portlet-body">
            <p><fmt:message key="rules.intro" /></p>

            <h2><fmt:message key="rules.section.scoring" /></h2>
            <ul>
                <li><fmt:message key="rules.item.scoring.exact" /></li>
                <li><fmt:message key="rules.item.scoring.result_bonus" /></li>
                <li><fmt:message key="rules.item.scoring.result" /></li>
                <li><fmt:message key="rules.item.scoring.partial" /></li>
                <li><fmt:message key="rules.item.scoring.none" /></li>
            </ul>

            <h2><fmt:message key="rules.section.deadlines" /></h2>
            <ul>
                <li><fmt:message key="rules.item.deadline.lock" /></li>
                <li><fmt:message key="rules.item.deadline.review" /></li>
                <li><fmt:message key="rules.item.deadline.timezone" /></li>
            </ul>

            <h2><fmt:message key="rules.section.tie" /></h2>
            <p><fmt:message key="rules.item.tie.breaker" /></p>
            <ul>
                <li><fmt:message key="rules.item.tie.total_points" /></li>
                <li><fmt:message key="rules.item.tie.full_hits" /></li>
                <li><fmt:message key="rules.item.tie.partial_bonus" /></li>
                <li><fmt:message key="rules.item.tie.alphabetic" /></li>
            </ul>

            <h2><fmt:message key="rules.section.goodpractices" /></h2>
            <ul>
                <li><fmt:message key="rules.item.goodpractices.security" /></li>
                <li><fmt:message key="rules.item.goodpractices.respect" /></li>
                <li><fmt:message key="rules.item.goodpractices.support" /></li>
            </ul>
        </div>
    </opendev:portlet>
</div>
