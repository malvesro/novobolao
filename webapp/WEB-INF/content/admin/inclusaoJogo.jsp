<%@include file="/WEB-INF/content/template/menu.jspf" %>
<%@taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<script type="text/javascript">
	const baseUrl = "${base}";
	const defaultErrorMessage = "<fmt:message key='match.submit.error' />";

	function resetFormularioCadastro() {
		const selects = [
			"data_select_id",
			"hora_select_id",
			"equipe1_select_id",
			"equipe2_select_id",
			"local_select_id",
			"fase_select_id"
		];
		selects.forEach(function(id) {
			const element = document.getElementById(id);
			if (element) {
				element.selectedIndex = 0;
			}
		});
	}

	function exibirMensagemErro(mensagem) {
		const spanErro = document.getElementById("errorSpan");
		if (!spanErro) {
			return;
		}
		spanErro.textContent = mensagem || defaultErrorMessage;
		spanErro.classList.remove("hidden");
		setTimeout(function() {
			spanErro.classList.add("hidden");
		}, 6000);
	}

	function destacarPortlet(portletId, classe) {
		const portlet = document.getElementById(portletId);
		if (!portlet) {
			return;
		}
		portlet.classList.remove("row-highlight--success", "row-highlight--error");
		void portlet.offsetWidth;
		portlet.classList.add(classe);
		setTimeout(function() {
			portlet.classList.remove(classe);
		}, 2000);
	}

	function submeterNovoJogo() {
		const params = new URLSearchParams({
			data: document.getElementById("data_select_id").value,
			hora: document.getElementById("hora_select_id").value,
			equipe1Id: document.getElementById("equipe1_select_id").value,
			equipe2Id: document.getElementById("equipe2_select_id").value,
			local: document.getElementById("local_select_id").value,
			fase: document.getElementById("fase_select_id").value
		});

		fetch(baseUrl + "/admin/criarJogo.action", {
			method: "POST",
			headers: { "Content-Type": "application/x-www-form-urlencoded" },
			body: params
		}).then(function(response) {
			if (response.ok) {
				resetFormularioCadastro();
				const errorSpan = document.getElementById("errorSpan");
				if (errorSpan) {
					errorSpan.classList.add("hidden");
				}
				destacarPortlet("cadastrojogo_portet_content", "row-highlight--success");
			} else if (response.status === 400) {
				exibirMensagemErro(defaultErrorMessage);
				destacarPortlet("cadastrojogo_portet_content", "row-highlight--error");
			} else {
				exibirMensagemErro("<fmt:message key='match.submit.error' />");
				destacarPortlet("cadastrojogo_portet_content", "row-highlight--error");
			}
		}).catch(function() {
			exibirMensagemErro("<fmt:message key='match.submit.error' />");
			destacarPortlet("cadastrojogo_portet_content", "row-highlight--error");
		});
	}
</script>
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
	</div>
</form>
</opendev:portlet>
</div>
