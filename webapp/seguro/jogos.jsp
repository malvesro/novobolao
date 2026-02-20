<%@include file="/template/menu.jspf" %>
<%@taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib prefix="opendev" uri="http://www.opendev.com.br/tld" %>

<script type="text/javascript">
	const baseUrl = "${base}";
	const arrowRightSrc = baseUrl + "/img/arrow_right.png";
	const arrowDownSrc = baseUrl + "/img/arrow_down.png";
	let idJogoSelecionado = null;
	let linhaSelecionada = null;
	let meusPalpitesCarregado = false;

	function collapseContainer(containerId, imgElement) {
		const content = document.getElementById(containerId + "_content");
		if (!content) {
			return;
		}
		const isHidden = content.classList.toggle("collapsible-portlet__content--hidden");
		imgElement.src = isHidden ? arrowRightSrc : arrowDownSrc;
	}

	function getElementPosition(element) {
		const rect = element.getBoundingClientRect();
		return {
			x: rect.left + window.scrollX,
			y: rect.top + window.scrollY
		};
	}

	function destacarLinha(row, highlight) {
		if (highlight) {
			row.dataset.originalClass = row.dataset.originalClass || row.className;
			if ((row.dataset.originalClass || "").indexOf("brasil") !== -1) {
				row.className = "destacado_brasil";
			} else {
				row.className = "destacado";
			}
		} else if (row.dataset.originalClass) {
			row.className = row.dataset.originalClass;
		}
	}

	function mostrarPopupPalpite(rowElement) {
		if (!rowElement) {
			return;
		}
		const podeDarPalpite = rowElement.dataset.palpiteAllowed === "true";
		const jogoId = Number(rowElement.dataset.jogoId);
		const palpiteDiv = document.getElementById("balao_palpite");
		const palpitesDiv = document.getElementById("balao_palpites");
		const statusContainer = document.getElementById("palpite-status");
		const loadingPalpite = document.getElementById("loading_span");
		const loadingPalpites = document.getElementById("loading_span_palpites");

		idJogoSelecionado = jogoId;
		linhaSelecionada = rowElement;

		if (statusContainer) {
			statusContainer.innerHTML = "";
		}

		if (loadingPalpite) {
			loadingPalpite.classList.remove("loading-inline--visible");
		}
		if (loadingPalpites) {
			loadingPalpites.classList.remove("loading-inline--visible");
		}

		if (palpiteDiv) {
			palpiteDiv.classList.remove("balao-visible");
		}
		if (palpitesDiv) {
			palpitesDiv.classList.remove("balao-visible");
		}

		const coords = getElementPosition(rowElement);

		if (podeDarPalpite) {
			if (palpiteDiv) {
				palpiteDiv.style.top = (coords.y - 118) + "px";
				palpiteDiv.style.left = (coords.x + 212) + "px";
				palpiteDiv.classList.add("balao-visible");
			}
			const gols1 = rowElement.dataset.palpiteGols1 || "";
			const gols2 = rowElement.dataset.palpiteGols2 || "";
			const inputGols1 = document.getElementById("palpite_gols_eq_1");
			const inputGols2 = document.getElementById("palpite_gols_eq_2");
			if (inputGols1) {
				inputGols1.value = gols1;
				inputGols1.focus();
			}
			if (inputGols2) {
				inputGols2.value = gols2;
			}
		} else {
			if (palpitesDiv) {
				palpitesDiv.style.top = (coords.y - 206) + "px";
				palpitesDiv.style.left = (coords.x + 212) + "px";
				palpitesDiv.classList.add("balao-visible");
			}
			carregarPalpitesDoJogo(jogoId);
		}
	}

	function carregarPalpitesDoJogo(jogoId) {
		const loading = document.getElementById("loading_span_palpites");
		if (loading) {
			loading.classList.add("loading-inline--visible");
		}
		const request = htmx.ajax("GET", baseUrl + "/seguro/palpitesDoJogoPartial.action", {
			target: "#balao_table_palpites",
			swap: "innerHTML",
			values: { jogoId: jogoId }
		});
		request.addEventListener("loadend", function() {
			if (loading) {
				loading.classList.remove("loading-inline--visible");
			}
		});
	}

	function atualizarPalpite() {
		if (idJogoSelecionado === null) {
			return;
		}
		const inputGols1 = document.getElementById("palpite_gols_eq_1");
		const inputGols2 = document.getElementById("palpite_gols_eq_2");
		if (!inputGols1 || !inputGols2) {
			return;
		}
		const gols1 = inputGols1.value === "" ? "0" : inputGols1.value;
		const gols2 = inputGols2.value === "" ? "0" : inputGols2.value;
		inputGols1.value = gols1;
		inputGols2.value = gols2;

		if (linhaSelecionada) {
			linhaSelecionada.dataset.palpiteGols1 = gols1;
			linhaSelecionada.dataset.palpiteGols2 = gols2;
		}

		const loading = document.getElementById("loading_span");
		if (loading) {
			loading.classList.add("loading-inline--visible");
		}

		const request = htmx.ajax("POST", baseUrl + "/seguro/atualizarPalpitePartial.action", {
			target: "#palpite-status",
			swap: "innerHTML",
			values: {
				jogoId: idJogoSelecionado,
				palpiteGolsEquipe1: gols1,
				palpiteGolsEquipe2: gols2
			}
		});

		request.addEventListener("loadend", function() {
			if (loading) {
				loading.classList.remove("loading-inline--visible");
			}
			if (request.status >= 200 && request.status < 300) {
				const meusPalpitesPainel = document.getElementById("todos_palpites_div");
				if (meusPalpitesPainel && meusPalpitesPainel.classList.contains("tips-panel--visible")) {
					carregarMeusPalpites(true);
				}
			}
		});
	}

	function atualizarResultado(input) {
		const jogoId = input.id.substring(input.id.lastIndexOf("_") + 1);
		const tr = document.getElementById("jogoTr_" + jogoId);
		const golsEq1Field = document.getElementById("golsEquipe1_tf_" + jogoId);
		const golsEq1 = golsEq1Field && golsEq1Field.value !== "" ? golsEq1Field.value : "-1";
		const golsEq2 = input.value !== "" ? input.value : "-1";

		fetch(baseUrl + "/admin/atualizarResultadoJogo.action", {
			method: "POST",
			headers: { "Content-Type": "application/x-www-form-urlencoded" },
			body: new URLSearchParams({
				id: jogoId,
				golsEquipe1: golsEq1,
				golsEquipe2: golsEq2
			})
		}).then(function(response) {
			destacarAtualizacao(tr, response.ok);
		}).catch(function() {
			destacarAtualizacao(tr, false);
		});
	}

	function destacarAtualizacao(row, sucesso) {
		if (!row) {
			return;
		}
		row.classList.remove("row-highlight--success", "row-highlight--error");
		void row.offsetWidth;
		row.classList.add(sucesso ? "row-highlight--success" : "row-highlight--error");
	}

	function fecharBalao() {
		const palpiteDiv = document.getElementById("balao_palpite");
		const palpitesDiv = document.getElementById("balao_palpites");
		const statusContainer = document.getElementById("palpite-status");
		if (palpiteDiv) {
			palpiteDiv.classList.remove("balao-visible");
		}
		if (palpitesDiv) {
			palpitesDiv.classList.remove("balao-visible");
		}
		if (statusContainer) {
			statusContainer.innerHTML = "";
		}
		const loadingPalpite = document.getElementById("loading_span");
		const loadingPalpites = document.getElementById("loading_span_palpites");
		if (loadingPalpite) {
			loadingPalpite.classList.remove("loading-inline--visible");
		}
		if (loadingPalpites) {
			loadingPalpites.classList.remove("loading-inline--visible");
		}
	}

	function fecharIconeMouseOver(img) {
		img.src = baseUrl + "/img/fechar_hover.gif";
	}

	function fecharIconeMouseOut(img) {
		img.src = baseUrl + "/img/fechar.gif";
	}

	function mostrarMeusPalpites() {
		const painel = document.getElementById("todos_palpites_div");
		if (!painel) {
			return;
		}
		painel.classList.add("tips-panel--visible");
		carregarMeusPalpites(false);
	}

	function fecharMeusPalpites() {
		const painel = document.getElementById("todos_palpites_div");
		if (painel) {
			painel.classList.remove("tips-panel--visible");
		}
	}

	function carregarMeusPalpites(force) {
		if (!force && meusPalpitesCarregado) {
			return;
		}
		const request = htmx.ajax("GET", baseUrl + "/seguro/meusPalpitesPartial.action", {
			target: "#todos_palpites_table",
			swap: "innerHTML"
		});
		request.addEventListener("loadend", function() {
			meusPalpitesCarregado = request.status >= 200 && request.status < 300;
		});
	}

	document.addEventListener("DOMContentLoaded", function() {
		const link = document.getElementById("mostrarMeusPalpitesLink");
		if (link) {
			link.addEventListener("click", function(event) {
				event.preventDefault();
				mostrarMeusPalpites();
			});
		}
	});
</script>

<div class="dashboard-section">
<form action="#" method="POST">

<c:set var="dataJogo" />
<c:set var="rowIndex" value="0" />

<c:if test="${telaPalpites}">
<!--
<embed src="${base}/fx/popup.wav" id="popup_show_sound" autoplay="false" autostart="false" loop="false" hidden="true"></embed>
-->
<div id="balao_palpite">
	<div class="balao_top">
		<p>
			<span id="loading_span" class="loading-inline">
				<img alt="" src="${base}/img/loading.gif" class="icon-inline-top" /> <fmt:message key="general.loading" />
			</span>
			<img alt="Fechar" src="${base}/img/fechar.gif" class="icon-inline-top icon-button"
				onclick="fecharBalao();" onmouseover="fecharIconeMouseOver(this);"
				onmouseout="fecharIconeMouseOut(this);" />
		</p>
	</div>
	<div class="balao_middle balao-middle--compact">
		<div id="palpite-status" class="balao-message"></div>
		<div class="text-center">
			<label for="palpite_gols_eq_1"><fmt:message key="match.tip.self" /></label>
			<input type="text" name="palpiteGolsEq1" id="palpite_gols_eq_1" class="text score-input input-centered" size="2" maxlength="2" />
			<span>X</span>
			<input type="text" name="palpiteGolsEq2" id="palpite_gols_eq_2" class="text score-input input-centered" size="2" maxlength="2" />
			<fmt:message var="palpiteSubmitLabel" key="match.tip.confirm" />
			<input type="button" class="button" name="confirmarPalpite" id="confirmar_palpite_button" value="${palpiteSubmitLabel}" onclick="atualizarPalpite();" />
		</div>
	</div>
	<div class="balao_bottom"></div>
</div>

<div id="balao_palpites">
	<div class="balao_top">
		<p>
			<span id="loading_span_palpites" class="loading-inline">
				<img alt="" src="${base}/img/loading.gif" class="icon-inline-top" /> <fmt:message key="general.loading" />
			</span>
			<img alt="Fechar" src="${base}/img/fechar.gif" class="icon-inline-top icon-button"
				onclick="fecharBalao();" onmouseover="fecharIconeMouseOver(this);"
				onmouseout="fecharIconeMouseOut(this);" />
		</p>
	</div>
		<div class="balao_middle balao-middle--scroll">
			<div class="balao-scroll">
			<table class="table tips-panel__table">
				<thead>
					<tr>
						<th scope="col"><fmt:message key="member" /></th>
						<th scope="col"><fmt:message key="match.tip" /></th>
						<th scope="col"><fmt:message key="match.tip.points" /></th>
					</tr>
			</thead>
			<tbody id="balao_table_palpites">
			</tbody>
		</table>
		</div>
	</div>
	<div class="balao_bottom"></div>
</div>

</c:if>

<c:if test="${telaPalpites}">
	<opendev:portlet id="filtro_jogos" title="Filtro de Busca" icon="/img/view.png">
		<c:url var="aplicarFiltroJogosActionURL" value="/seguro/palpites.action" />
		<form action="${aplicarFiltroJogosActionURL}" method="get">
        <input type="hidden" name="usarFiltro" value="true" />
		<div class="match-filter">
		<table class="match-filter__table">
			<tr>
				<td class="label"><label for="filtro_time"><fmt:message key="filter.dates" /></label></td>
				<td class="widget match-filter__row">
					<select name="dataInicial" id="data_select_id">
						<option value=""><fmt:message key="filter.fase.0" /></option>
					<c:forTokens var="data" delims="," items="${initParam.datas}">
						<c:choose>
							<c:when test="${not empty filtro and filtro.dataInicialFormatada eq data}">
								<option value="${data}" selected="selected">${data}</option>
							</c:when>
							<c:otherwise>
								<option value="${data}">${data}</option>
							</c:otherwise>
						</c:choose>
					</c:forTokens>
					</select>
					<fmt:message key="filter.dates.and" />
					<select name="dataFinal" id="data_select_id">
						<option value=""><fmt:message key="filter.fase.0" /></option>
					<c:forTokens var="data" delims="," items="${initParam.datas}">
						<c:choose>
							<c:when test="${not empty filtro and filtro.dataFinalFormatada eq data}">
								<option value="${data}" selected="selected">${data}</option>
							</c:when>
							<c:otherwise>
								<option value="${data}">${data}</option>
							</c:otherwise>
						</c:choose>
					</c:forTokens>
					</select>
				</td>
			</tr>
			<tr>
				<td class="label"><label for="filtro_time"><fmt:message key="filter.team" /></label></td>
				<td class="widget">
					<select name="filtroEquipe" id="filtro_equipe">
						<option value=""><fmt:message key="filter.fase.0" /></option>
					<c:forEach var="equipe" items="${equipes}">
						<c:if test="${not empty grupoAnterior and grupoAnterior ne equipe.grupo}">
							</optgroup>
						</c:if>
						<c:if test="${empty grupoAnterior or (grupoAnterior ne equipe.grupo)}">
							<optgroup label="Grupo ${equipe.grupo}">
						</c:if>
							<c:choose>
								<c:when test="${not empty filtro and filtro.idEquipe eq equipe.id}">
									<option value="${equipe.id}" selected="selected">${equipe.nomePais}</option>
								</c:when>
								<c:otherwise>
									<option value="${equipe.id}">${equipe.nomePais}</option>
								</c:otherwise>
							</c:choose>
						<c:set var="grupoAnterior" value="${equipe.grupo}" />
					</c:forEach>
					</select>
				</td>
			</tr>
			<tr>
				<td class="label"><label for="filtro_grupo"><fmt:message key="filter.group" /></label></td>
				<td class="widget">
					<select name="filtroGrupo" id="filtro_grupo">
						<option value=""><fmt:message key="filter.group.0" /></option>
						<c:forTokens var="grupo" items="A,B,C,D,E,F,G,H" delims=",">
							<c:choose>
								<c:when test="${not empty filtro and filtro.grupo eq grupo}">
									<option value="${grupo}" selected="selected">${grupo}</option>
								</c:when>
								<c:otherwise>
									<option value="${grupo}">${grupo}</option>
								</c:otherwise>
							</c:choose>							
						</c:forTokens>
					</select>
				</td>
			</tr>
			<tr>
				<td class="label"><label for="filtro_fase"><fmt:message key="filter.fase" /></td>
				<td class="widget">
					<select name="filtroFase" id="filtro_fase">
						<option value=""><fmt:message key="filter.fase.0" /></option>
						<c:forTokens var="fase" items="11,12,13,8,4,2,3,1" delims=",">
							<c:choose>
								<c:when test="${not empty filtro and filtro.fase eq fase}">
									<option value="${fase}" selected="selected"><fmt:message key="filter.fase.${fase}" /></option>
								</c:when>
								<c:otherwise>
									<option value="${fase}"><fmt:message key="filter.fase.${fase}" /></option>
								</c:otherwise>
							</c:choose>							
						</c:forTokens>
					</select>
				</td>
			</tr>
			<tr>
				<td><br /></td>
				<td class="widget match-filter__row">
                    <c:choose>
                        <c:when test="${not empty filtro and filtro.soSemPalpite}">
                            <input type="checkbox" id="filtro_sem_palpite_cb" name="filtroSemPalpite" value="true" checked="checked" />
                        </c:when>
                        <c:otherwise>
                            <input type="checkbox" id="filtro_sem_palpite_cb" name="filtroSemPalpite" value="true" />
                        </c:otherwise>
                    </c:choose>
					<label for="filtro_sem_palpite_cb"><fmt:message key="filter.withouttip" /></label>
				</td>
			</tr>
			<tr>
				<td><br /></td>
				<td class="widget match-filter__row">
                    <c:choose>
                    <c:when test="${not empty filtro and filtro.soJogosQueNaoOcorreram}">
                        <input type="checkbox" id="filtro_jogos_ocorreram_cb" name="filtroJogosNaoOcorreram" value="true" checked="checked" />
                    </c:when>
                    <c:otherwise>
                        <input type="checkbox" id="filtro_jogos_ocorreram_cb" name="filtroJogosNaoOcorreram" value="true" />
                    </c:otherwise>
                    </c:choose>
					<label for="filtro_jogos_ocorreram_cb"><fmt:message key="filter.notplayed" /></label>
				</td>
			</tr>
			<%--
			<tr>
				<td><br /></td>
				<td class="widget">
					<input type="checkbox" id="filtro_jogosagora_cb" name="jogosAgora" value="true" checked="checked" />
					<label for="filtro_sem_palpite_cb"><fmt:message key="filter.matchs.now" /></label>
				</td>
			</tr>
			--%>
		</table>
		</div>
		<div class="match-filter__actions">
			<fmt:message var="filterSubmitLabel" key="filter.submit" />
			<input type="submit" name="submit" class="button" value="${filterSubmitLabel}" />
		</div>
		</form>
	</opendev:portlet>
	<span class="spacer spacer-sm"></span>
    <div class="tips-trigger">
    	<img alt="" src="${base}/img/triang_yellow.png" class="icon-inline" />
    	<a href="#" id="mostrarMeusPalpitesLink">Ver meus palpites</a>
    </div>
    <div id="todos_palpites_div" class="tips-panel">
    	<div class="tips-panel__body">
    		<div id="todos_palpites_loading" class="tips-panel__header">
    			<button type="button" class="link-button" onclick="fecharMeusPalpites();"><fmt:message key="match.tip.close" /></button>
    		</div>
    		<div class="tips-panel__footer">
    			<p><fmt:message key="match.tip.now" /></p>
    			<img alt="" src="${base}/img/refresh.png" class="icon-inline icon-button" onclick="carregarMeusPalpites(true);" />
    		</div>
    		<div class="tips-panel__scroll">
    		<table class="table tips-panel__table">
    			<thead>
	    				<tr>
	    					<th scope="col"><fmt:message key="match.tip.date" /></th>
	    					<th scope="col"><fmt:message key="match.tip.hour" /></th>
	    					<th scope="col"><fmt:message key="match.tip.teams" /></th>
	    					<th scope="col"><fmt:message key="match.tip.mine" /></th>
	    				</tr>
    			</thead>
    			<tbody id="todos_palpites_table">
    			</tbody>
    		</table>
    		</div>
    		<span class="spacer spacer-sm"></span>
    	</div>
    </div>
    <span class="spacer spacer-sm"></span>
    <div id="palpites_info" class="legenda tips-info">
        <p><img alt="" src="${base}/img/information.gif" class="icon-inline" />
            <fmt:message key="match.tip.help" />
        </p>
    </div>
    <span class="spacer spacer-sm"></span>
</c:if>

<c:forEach var="jogo" items="${jogos}" varStatus="loop">
	<fmt:formatDate var="dataJogoFormatada" value="${jogo.data}" pattern="dd/MM/yyyy" />
	<fmt:formatDate var="horaJogoFormatada" value="${jogo.hora}" pattern="HH:mm" />
	<c:if test="${not empty dataJogo and dataJogo ne jogo.data}">
		<c:set var="rowIndex" value="0" />
					</tbody>
				</table>
			</div>
		</div>
		<span class="spacer spacer-sm"></span>
	</c:if>
	<c:if test="${empty dataJogo or dataJogo ne jogo.data}">
		<fmt:formatDate var="dataJogoFormatada" value="${jogo.data}" pattern="dd/MM/yyyy" />
		<div id="jogos_${dataJogoFormatada}_portlet" class="portlet collapsible-portlet">
			<div class="title collapsible-portlet__header">
				<img alt="" src="${base}/img/arrow_down.png" class="collapse-toggle icon-inline-top icon-button" onclick="collapseContainer('jogos_${dataJogoFormatada}_portlet', this)" />
				<fmt:message key="matchs.day">
					<fmt:param value="${dataJogoFormatada}" />
				</fmt:message>
			</div>
				<div class="content collapsible-portlet__content" id="jogos_${dataJogoFormatada}_portlet_content">
					<div class="table-responsive">
					<table class="table conteudo">
						<thead>
							<tr>
								<th scope="col"><fmt:message key="match.hour" /></th>
								<th scope="col"><fmt:message key="match.where" /></th>
								<th scope="col"><fmt:message key="match.group" /></th>
								<th scope="colgroup" colspan="3"><fmt:message key="match.teams" /></th>
						</tr>
					</thead>
						<tbody>
			</c:if>
			<c:choose>
				<c:when test="${jogo.equipe1.nomePais eq 'Brasil' or jogo.equipe2.nomePais eq 'Brasil'}">
					<c:set var="rowStyleClass" value="brasil" />
			</c:when>
			<c:when test="${rowIndex mod 2 eq 0}">
				<c:set var="rowStyleClass" value="impar" />
			</c:when>
			<c:otherwise>
				<c:set var="rowStyleClass" value="par" />
			</c:otherwise>
		</c:choose>
			<c:choose>
				<c:when test="${telaPalpites}">
					<c:set var="palpiteUsuario" value="${palpitesUsuario[jogo.id]}" />
					<c:set var="palpiteGols1Attr" value="" />
					<c:set var="palpiteGols2Attr" value="" />
					<c:if test="${not empty palpiteUsuario}">
						<c:set var="palpiteGols1Attr" value="${palpiteUsuario.golsEquipe1}" />
						<c:set var="palpiteGols2Attr" value="${palpiteUsuario.golsEquipe2}" />
					</c:if>
					<authz:authorize ifAllGranted="geral">
					<tr class="${rowStyleClass}" id="jogoTr_${jogo.id}" data-jogo-id="${jogo.id}" data-palpite-allowed="${jogo.podeDarPalpite}" data-palpite-gols1="${palpiteGols1Attr}" data-palpite-gols2="${palpiteGols2Attr}" onmouseover="destacarLinha(this, true);" onmouseout="destacarLinha(this, false);" onclick="mostrarPopupPalpite(this);">
					</authz:authorize>
					<authz:authorize ifAllGranted="restrito">
					<tr class="${rowStyleClass}" id="jogoTr_${jogo.id}">
					</authz:authorize>
				</c:when>
				<c:otherwise>
					<tr class="${rowStyleClass}" id="jogoTr_${jogo.id}">
				</c:otherwise>
			</c:choose>
						<td class="match-table__time">${horaJogoFormatada}</td>
						<td class="match-table__location">${jogo.local}</td>
						<td class="match-table__group">${jogo.equipe1.grupo}</td>
						<td class="match-table__team match-table__team--home">
							<div class="team-cell text-right">
								<span>${jogo.equipe1.nomePais}</span>
								<img alt="" src="${base}/img/bandeiras/${jogo.equipe1.id}.gif" class="icon-inline" />
								<c:choose>
									<c:when test="${not telaPalpites}">
										<authz:authorize ifAllGranted="admin">
											<input type="text" name="golsEquipe1" id="golsEquipe1_tf_${jogo.id}" class="text score-input input-centered" maxlength="2" size="2" value="${jogo.golsEquipe1}" />
										</authz:authorize>
									</c:when>
									<c:otherwise>
										<span class="score-value">${jogo.golsEquipe1}</span>
									</c:otherwise>
								</c:choose>
							</div>
						</td>
						<td class="match-table__separator">X</td>
						<td class="match-table__team match-table__team--away">
							<div class="team-cell text-left">
								<c:choose>
								<c:when test="${not telaPalpites}">
									<authz:authorize ifAllGranted="admin">
										<input type="text" name="golsEquipe2" id="golsEquipe2_tf_${jogo.id}" class="text score-input input-centered" maxlength="2" size="2" value="${jogo.golsEquipe2}" onblur="atualizarResultado(this);" />
									</authz:authorize>
								</c:when>
								<c:otherwise>
									<span class="score-value">${jogo.golsEquipe2}</span>
								</c:otherwise>
								</c:choose>
								<img alt="" src="${base}/img/bandeiras/${jogo.equipe2.id}.gif" class="icon-inline" />
								<span>${jogo.equipe2.nomePais}</span>
							</div>
						</td>
					</tr>
	<c:set var="dataJogo" value="${jogo.data}" />
	<c:if test="${loop.count eq fn:length(jogos)}">
					</tbody>
				</table>
			</div>
		</div>		
	</c:if>
	<c:set var="rowIndex" value="${rowIndex + 1}" />
</c:forEach>
</form>
<span class="spacer spacer-sm"></span>
</div>
