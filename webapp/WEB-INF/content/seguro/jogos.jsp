<%@include file="/WEB-INF/content/template/menu.jspf" %>
<%@taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib prefix="opendev" uri="http://www.opendev.com.br/tld" %>



<div id="jogos-page-wrapper" class="dashboard-section">
<form action="#" method="POST">

<c:set var="dataJogo" />
<c:set var="rowIndex" value="0" />

<c:if test="${telaPalpites}">
<!--
<embed src="${base}/fx/popup.wav" id="popup_show_sound" autoplay="false" autostart="false" loop="false" hidden="true"></embed>
-->
<div id="balao_palpite" class="dialog" role="dialog" aria-modal="true" aria-labelledby="palpite-dialog-title" aria-describedby="palpite-dialog-description" aria-hidden="true">
	<div class="balao_top">
		<p>
			<span id="loading_span" class="loading-inline" role="status" aria-live="polite">
				<img alt="" src="${base}/img/loading.gif" class="icon-inline-top" aria-hidden="true" /> <fmt:message key="general.loading" />
			</span>
			<img alt="Fechar" src="${base}/img/fechar.gif" class="icon-inline-top icon-button" data-js="fechar-balao" />
		</p>
	</div>
	<div class="balao_middle balao-middle--compact">
		<h2 id="palpite-dialog-title" class="sr-only"><fmt:message key="match.tip.self" /></h2>
		<p id="palpite-dialog-description" class="sr-only">Informe seus palpites de gols para cada equipe e confirme.</p>
		<div id="palpite-status" class="balao-message" role="status" aria-live="assertive"></div>
		<div class="text-center">
			<label for="palpite_gols_eq_1"><fmt:message key="match.tip.self" /></label>
			<input type="text" name="palpiteGolsEq1" id="palpite_gols_eq_1" class="text score-input input-centered" size="2" maxlength="2" />
			<span>X</span>
			<input type="text" name="palpiteGolsEq2" id="palpite_gols_eq_2" class="text score-input input-centered" size="2" maxlength="2" />
			<fmt:message var="palpiteSubmitLabel" key="match.tip.confirm" />
			<input type="button" class="button" name="confirmarPalpite" id="confirmar_palpite_button" value="${palpiteSubmitLabel}" />
		</div>
	</div>
	<div class="balao_bottom"></div>
</div>

<div id="balao_palpites" class="dialog" role="dialog" aria-modal="true" aria-labelledby="palpites-dialog-title" aria-hidden="true">
	<div class="balao_top">
		<p>
			<span id="loading_span_palpites" class="loading-inline" role="status" aria-live="polite">
				<img alt="" src="${base}/img/loading.gif" class="icon-inline-top" aria-hidden="true" /> <fmt:message key="general.loading" />
			</span>
			<img alt="Fechar" src="${base}/img/fechar.gif" class="icon-inline-top icon-button" data-js="fechar-balao" />
		</p>
	</div>
		<div class="balao_middle balao-middle--scroll">
			<div class="balao-scroll">
			<h2 id="palpites-dialog-title" class="sr-only">Palpites dos participantes</h2>
			<table class="table tips-panel__table" aria-describedby="palpites-dialog-title">
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
		<table class="match-filter__table" role="presentation">
			<tr>
				<td class="label"><label for="data_inicial_select"><fmt:message key="filter.dates" /></label></td>
				<td class="widget match-filter__row">
					<select name="dataInicial" id="data_inicial_select">
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
					<select name="dataFinal" id="data_final_select">
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
				<td class="label"><label for="filtro_equipe"><fmt:message key="filter.team" /></label></td>
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
    			<button type="button" class="link-button" data-js="fechar-meus-palpites"><fmt:message key="match.tip.close" /></button>
    		</div>
    		<div class="tips-panel__footer">
    			<p><fmt:message key="match.tip.now" /></p>
    			<img alt="" src="${base}/img/refresh.png" class="icon-inline icon-button" data-js="recarregar-meus-palpites" />
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
		<p><img alt="" src="${base}/img/information.gif" class="icon-inline" aria-hidden="true" />
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
				<img alt="Alternar exibição do filtro" src="${base}/img/arrow_down.png" class="collapse-toggle icon-inline-top icon-button"
					data-js="collapse-container" data-target="jogos_${dataJogoFormatada}_portlet" />
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
					<tr class="${rowStyleClass}" id="jogoTr_${jogo.id}" data-jogo-id="${jogo.id}" data-palpite-allowed="${jogo.podeDarPalpite}" data-palpite-gols1="${palpiteGols1Attr}" data-palpite-gols2="${palpiteGols2Attr}">
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
								<img alt="${jogo.equipe1.nomePais}" src="${base}/img/bandeiras/${jogo.equipe1.id}.gif" class="icon-inline" />
								<c:choose>
									<c:when test="${not telaPalpites}">
										<authz:authorize ifAllGranted="admin">
											<input type="text" name="golsEquipe1" id="golsEquipe1_tf_${jogo.id}" class="text score-input input-centered" maxlength="2" size="2" value="${jogo.golsEquipe1}" data-js="resultado-input" />
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
										<input type="text" name="golsEquipe2" id="golsEquipe2_tf_${jogo.id}" class="text score-input input-centered" maxlength="2" size="2" value="${jogo.golsEquipe2}" onblur="atualizarResultado(this);" data-js="resultado-input" />
									</authz:authorize>
								</c:when>
								<c:otherwise>
									<span class="score-value">${jogo.golsEquipe2}</span>
								</c:otherwise>
								</c:choose>
								<img alt="${jogo.equipe2.nomePais}" src="${base}/img/bandeiras/${jogo.equipe2.id}.gif" class="icon-inline" />
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
