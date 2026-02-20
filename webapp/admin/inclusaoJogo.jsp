<%@include file="/template/menu.jspf" %>
<%@taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<script type="text/javascript" src="${base}/dwr/interface/AdminAction.js"></script>
<div class="dashboard-section">
<span class="error hidden mb-md" id="errorSpan">
</span>
<opendev:portlet id="cadastrojogo_portet" title="Cadastro de Jogo">
<form action="#">
		<div class="inner">
			<div class="form-grid form-grid--auto">
				<div class="form-row">
					<label for="data_select_id"><fmt:message key="match.day" /></label>
					<select name="data" id="data_select_id">
						<option value=""><fmt:message key="filter.fase.0" /></option>
						<c:forTokens var="data" delims="," items="${initParam.datas}">
							<option value="${data}">${data}</option>
						</c:forTokens>
					</select>
				</div>
				<div class="form-row">
					<label for="hora_select_id"><fmt:message key="match.hour" /></label>
					<select name="hora" id="hora_select_id">
						<option value=""><fmt:message key="filter.group.0" /></option>
						<c:forTokens var="horario" delims="," items="${initParam.horarios}">
							<option value="${horario}">${horario}</option>
						</c:forTokens>
					</select>
				</div>
				<div class="form-row form-row--full">
					<label for="equipe1_select_id"><fmt:message key="match.teams" /></label>
					<div class="form-field-group">
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
						<span class="form-field-separator">X</span>
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
					</div>
				</div>
				<div class="form-row form-row--full">
					<label for="local_select_id"><fmt:message key="match.where" /></label>
					<select name="local" id="local_select_id">
						<option value=""><fmt:message key="filter.group.0" /></option>
						<c:forTokens var="local" delims="," items="${initParam.locais}">
							<option value="${local}">${local}</option>
						</c:forTokens>
					</select>
				</div>
				<div class="form-row form-row--full">
					<label for="fase_select_id"><fmt:message key="match.phase" /></label>
					<select name="local" id="fase_select_id">
						<option value=""><fmt:message key="filter.fase.0" /></option>
						<c:forTokens var="fase" items="11,12,13,8,4,2,3,1" delims=",">
							<option value="${fase}"><fmt:message key="filter.fase.${fase}" /></option>
						</c:forTokens>
					</select>
				</div>
			</div>
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
