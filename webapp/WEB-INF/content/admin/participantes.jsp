<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="opendev" uri="http://www.opendev.com.br/tld" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@include file="/WEB-INF/content/template/menu.jspf" %>
<div class="dashboard-section">
<opendev:portlet id="participantesPortlet" title="Participantes" icon="/img/users.png">
    <div class="table-responsive">
    <table class="table conteudo table-participants">
        <thead>
            <tr>
                <th scope="col" class="text-center">#</th>
                <th scope="col"><fmt:message key="member.name" /></th>
                <th scope="col"><fmt:message key="member.login" /></th>
                <th scope="col"><fmt:message key="member.email" /></th>
                <th scope="col" class="text-center"><fmt:message key="member.role" /></th>
                <th scope="col" class="text-center"><fmt:message key="member.status" /></th>
                <th scope="col" class="text-center"><span class="visually-hidden">Ações</span></th>
            </tr>
        </thead>
        <tbody id="participantesTableBody">
        <%@ include file="/WEB-INF/content/admin/partials/participantes-rows.jspf" %>
        </tbody>
    </table>
    </div>
</opendev:portlet>
</div>
