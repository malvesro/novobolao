<%@include file="/template/menu.jspf" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="opendev" uri="http://www.opendev.com.br/tld" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<script type="text/javascript" src="${base}/dwr/interface/AdminAction.js"></script>
<script type="text/javascript">
function atualizarPapel(id) {
    var callBackFunc = function() {
        var trPar = $("linhaParticipante_" + id);
        new Effect.Highlight(trPar);
    };
    AdminAction.atualizarPapelParticipante(
        id,
        DWRUtil.getValue("papel_par_" + id),
        {callback: callBackFunc}
    );
}

function autorizarParticipante(id) {
    var callBackFunc = function() {
        var trPar = $("linhaParticipante_" + id);
        new Effect.Highlight(trPar);
    };
    AdminAction.autorizarParticipante(
        id,
        DWRUtil.getValue("status_par_" + id) === "Sim",
        {callback: callBackFunc}
    );
}
</script>
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
        <tbody>
            <c:forEach var="participante" items="${participantes}" varStatus="loop">
        		<c:choose>
        			<c:when test="${loop.count mod 2 eq 0}">
        				<c:set var="rowStyleClass" value="impar" />
        			</c:when>
        			<c:otherwise>
        				<c:set var="rowStyleClass" value="par" />
        			</c:otherwise>
        		</c:choose>
                <tr id="linhaParticipante_${participante.id}" class="${rowStyleClass}">
                    <td class="text-center">${loop.count}</td>
                    <td class="text-left"><c:out value="${participante.nomeFormatado}" /></td>
                    <td class="text-left"><c:out value="${participante.login}" /></td>
                    <td class="text-left"><c:out value="${participante.email}" /></td>
                    <td class="text-center">
                        <select id="papel_par_${participante.id}" name="papel" onchange="atualizarPapel(${participante.id});" class="form-control input-centered table-participants__select" aria-label="Papel do participante ${participante.login}">
                            <c:forTokens var="nivel" items="Nenhum,admin,geral,restrito" delims=",">
                                <c:choose>
                                    <c:when test="${nivel eq participante.privilegio.papel}">
                                        <option value="${nivel}" selected="selected">${nivel}</option>
                                    </c:when>
                                    <c:otherwise>
                                        <option value="${nivel}">${nivel}</option>
                                    </c:otherwise>
                                </c:choose>
                            </c:forTokens>
                        </select>
                    </td>
                    <td class="text-center">
                        <select id="status_par_${participante.id}" name="status" onchange="autorizarParticipante(${participante.id});" class="form-control input-centered table-participants__select" aria-label="Status do participante ${participante.login}">
                            <c:forTokens var="status" items="Sim,Não" delims=",">
                                <c:choose>
                                    <c:when test="${(status eq 'Sim' and participante.habilitado) or (status eq 'Não' and not participante.habilitado)}">
                                        <option value="${status}" selected="selected">${status}</option>
                                    </c:when>
                                    <c:otherwise>
                                        <option value="${status}">${status}</option>
                                    </c:otherwise>
                                </c:choose>
                            </c:forTokens>
                        </select>
                    </td>
                    <td class="text-center">
                        <img alt="Remover participante" src="${base}/img/delete.png" hx-post="${base}/admin/apagarParticipanteHtmx.action?id=${participante.id}" hx-target="#linhaParticipante_${participante.id}" hx-swap="delete" hx-confirm="Deseja realmente apagar este participante?" class="icon-button" />
                    </td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
    </div>
</opendev:portlet>
</div>
