<%@include file="/template/menu.jspf" %>
<%@taglib prefix="ww" uri="/webwork" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<script type="text/javascript" src="${base}/dwr/interface/AdminAction.js"></script>
<div style="float: right; width: 605px;">
<span class="error" id="errorSpan" style="display: none; margin-bottom: 15px;">
</span>
<opendev:portlet id="cadastrojogo_portet" title="Cadastro de Jogo" style="width: 450px; margin: 0px auto;">
<form action="#">
	<div class="inner">
		<table class="form" cellspacing="0" cellpadding="2" style="width:100%; margin-top: 10px; margin-bottom: 10px;">
			<tr>
				<td class="label"><label for=""><fmt:message key="match.day" /></label></td>
				<td class="widget">
					<select name="data" id="data_select_id">
						<option value=""><fmt:message key="filter.fase.0" /></option>
					<c:forTokens var="data" delims="," items="${initParam.datas}">
						<option value="${data}">${data}</option>
					</c:forTokens>
					</select>
				</td>
				<td class="label"><label for=""><fmt:message key="match.hour" /></label></td>
				<td class="widget">
					<select name="hora" id="hora_select_id">
						<option value=""><fmt:message key="filter.group.0" /></option>
					<c:forTokens var="horario" delims="," items="${initParam.horarios}">
						<option value="${horario}">${horario}</option>
					</c:forTokens>
					</select>
				</td>
			</tr>
			<tr>
				<td  class="label"><label for="equipe1_select_id"><fmt:message key="match.teams" /></label></td>
				<td colspan="3" class="widget">
					<select name="equipe1" id="equipe1_select_id">
						<option value=""><fmt:message key="filter.group.0" /></option>
					<c:forEach var="equipe" items="${equipes}">
						<c:if test="${not empty grupoAnterior and grupoAnterior ne equipe.grupo}">
							</optgroup>
						</c:if>
						<c:if test="${empty grupoAnterior or (grupoAnterior ne equipe.grupo)}">
							<optgroup label="Grupo ${equipe.grupo}">
						</c:if>
							<option value="${equipe.id}">${equipe.nomePais}</option>
						<c:set var="grupoAnterior" value="${equipe.grupo}" />
					</c:forEach>
					</select>
					X
					<c:set var="grupoAnterior" value="" />
					<select name="equipe2" id="equipe2_select_id">
						<option value=""><fmt:message key="filter.group.0" /></option>
					<c:forEach var="equipe" items="${equipes}">
						<c:if test="${not empty grupoAnterior and grupoAnterior ne equipe.grupo}">
							</optgroup>
						</c:if>
						<c:if test="${empty grupoAnterior or (grupoAnterior ne equipe.grupo)}">
							<optgroup label="Grupo ${equipe.grupo}">
						</c:if>
							<option value="${equipe.id}">${equipe.nomePais}</option>
						<c:set var="grupoAnterior" value="${equipe.grupo}" />
					</c:forEach>
					</select>
				</td>
			</tr>
			<tr>
				<td  class="label"><label for="equipe1_combo_id"><fmt:message key="match.where" /></label></td>
				<td colspan="3" class="widget">
					<select name="local" id="local_select_id">
						<option value=""><fmt:message key="filter.group.0" /></option>
					<c:forTokens var="local" delims="," items="${initParam.locais}">
						<option value="${local}">${local}</option>
					</c:forTokens>
					</select>
				</td>
			</tr>
			<tr>
				<td  class="label"><label for="equipe1_combo_id"><fmt:message key="match.phase" /></label></td>
				<td colspan="3" class="widget">
					<select name="local" id="fase_select_id">
						<option value=""><fmt:message key="filter.fase.0" /></option>
					<c:forTokens var="fase" items="11,12,13,8,4,2,3,1" delims=",">
						<option value="${fase}"><fmt:message key="filter.fase.${fase}" /></option>
					</c:forTokens>
					</select>
				</td>
			</tr>
		</table>
	</div>
	<div class="footer">
		<fmt:message var="loginSubmitLabel" key="match.submit" />
		<input type="button" name="submit" class="button" value="${loginSubmitLabel}" onclick="submeterNovoJogo();"; />
		<fmt:message var="errorMsg" key="match.submit.error" />
		<script type="text/javascript">
			function submeterNovoJogo() {
				var callbackFunc = function() {
					$("data_select_id").selectedIndex = 0;
					$("hora_select_id").selectedIndex = 0;
					$("equipe1_select_id").selectedIndex = 0;
					$("equipe2_select_id").selectedIndex = 0;
					$("local_select_id").selectedIndex = 0;
                    $("fase_select_id").selectedIndex = 0;
					new Effect.Highlight($("cadastrojogo_portet_content"));
				}
				var errorCallbackFunc = function(exMsg, ex) {
					var errorTimeout = 0;
					DWRUtil.setValue($("errorSpan"), "${errorMsg}");
					Effect.Appear($("errorSpan"));
					new Effect.Highlight($("cadastrojogo_portet_content"), {startcolor: "#FFE6DF"});
					var fadeFunc = function() {
						Effect.Fade($("errorSpan"));
						window.clearTimeout(errorTimeout);
					};
					errorTimeout = window.setTimeout(fadeFunc, 6000);
				}
				AdminAction.criarNovoJogo($("data_select_id").value,
					$("hora_select_id").value,$("equipe1_select_id").value,
					$("equipe2_select_id").value,$("local_select_id").value,
					$("fase_select_id").value,{callback:callbackFunc, errorHandler:errorCallbackFunc});
			}
		</script>
	</div>
</form>
</opendev:portlet>
</div>