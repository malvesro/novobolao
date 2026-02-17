<%@include file="/template/menu.jspf" %>
<%@taglib prefix="ww" uri="/struts-tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="authz" uri="http://acegisecurity.org/authz" %>
<%@taglib prefix="opendev" uri="http://www.opendev.com.br/tld" %>

<script type="text/javascript">
	function collapseContainer(containerId, imgElement) {
		var id = containerId + "_content";
		Effect.toggle($(id), "slide");
		var arrowRightSrc = "${base}/img/arrow_right.png";
		var arrowDownSrc = "${base}/img/arrow_down.png";
		if ($(id).style.display == "none") {
			imgElement.src = arrowDownSrc;
		} else {
			imgElement.src = arrowRightSrc;
		}
		
	} 
</script>

<authz:authorize ifAllGranted="admin">
<script type="text/javascript" src="${base}/dwr/interface/AdminAction.js"></script>

<script type="text/javascript">
<!--
	function atualizarResultado(tfGols2) {
		var jogoId = tfGols2.id.substring(tfGols2.id.lastIndexOf("_") + 1);
		var trId = "jogoTr_" + jogoId;
		var tfGols1 = "golsEquipe1_tf_" + jogoId;
		var callbackFunc = function() {
			new Effect.Highlight($(trId));
		};
		var errorCallbackFunc = function() {
			new Effect.Highlight($(trId), {startcolor: "#FFE6DF"});
		};
		var golsEq1 = $(tfGols1).value;
		var golsEq2 = tfGols2.value;
		if (golsEq1 == "") golsEq1 = "-1";
		if (golsEq2 == "") golsEq2 = "-1";
		
		AdminAction.atualizarResultadoDoJogo(
			jogoId, golsEq1, golsEq2,
			{callback:callbackFunc, errorHandler:errorCallbackFunc});
	}
//-->
</script>
</authz:authorize>

<div style="float: right; width: 605px;">
<form action="#" method="POST">

<c:set var="dataJogo" />
<c:set var="rowIndex" value="0" />

<c:if test="${telaPalpites}">
<authz:authorize ifAllGranted="geral">
<script type="text/javascript" src="${base}/dwr/interface/ParticipanteAction.js"></script>
<script type="text/javascript">
	var idJogoSelecionado = 0;
	var classNameAnterior = null;
	
	function getPosition(theElement) {
		var positionX = 0;
		var positionY = 0;

		while (theElement != null) {
			positionX += theElement.offsetLeft;
			positionY += theElement.offsetTop;
			theElement = theElement.offsetParent;
		}
		return {x: positionX, y: positionY};
	}
	
	function destacarLinha(cedula, flag) {
		var cn = null;
		if (classNameAnterior != null) {
			cn = classNameAnterior;
		} else {
			cn = cedula.currentStyle;
		}
		
		if (flag) {
			classNameAnterior = cedula.className;
			if (classNameAnterior == "brasil") {
				cn = "destacado_brasil";
			} else {
				cn = "destacado";
			}
		}
		cedula.className = cn;
	}
	
	function mostrarPopupPalpite(refPosEl, idJogo, podeDarPalpite) {
		if (podeDarPalpite) {
    		var palpiteDiv = $("balao_palpite");
    		Element.setOpacity(palpiteDiv, 0.90);
    		Element.setStyle(palpiteDiv, {visibility: "hidden"});
    		Element.setStyle($("balao_palpites"), {visibility: "hidden"});
    		var posicaoElemento = getPosition(refPosEl);
    		DWRUtil.setValue("palpite_gols_eq_1", "");
    		DWRUtil.setValue("palpite_gols_eq_2", "");
            //$("popup_show_sound").play();    
    		Element.setStyle(palpiteDiv, {top: (posicaoElemento.y - 118) + "px", left: (posicaoElemento.x + 212) + "px", visibility: "visible"});
    		$("palpite_gols_eq_1").focus();
    		idJogoSelecionado = idJogo;
    		var buscarPalpiteCallback = function(palpite) {
    			if (palpite != null) {
    				DWRUtil.setValue("palpite_gols_eq_1", palpite.golsEquipe1);
    				DWRUtil.setValue("palpite_gols_eq_2", palpite.golsEquipe2);
    			}
    			Element.setStyle($("loading_span"), {visibility: "hidden"});
    		}
    		Element.setStyle($("loading_span"), {visibility: "visible"});
    		ParticipanteAction.buscarPalpiteDoJogo(idJogo, {callback: buscarPalpiteCallback});
		} else {
    		var palpitesDiv = $("balao_palpites");
    		Element.setOpacity(palpitesDiv, 0.90);
    		Element.setStyle(palpitesDiv, {visibility: "hidden"});
    		Element.setStyle($("balao_palpite"), {visibility: "hidden"});
    		var posicaoElemento = getPosition(refPosEl);
    		DWRUtil.removeAllRows("balao_table_palpites");
    		Element.setStyle(palpitesDiv, {top: (posicaoElemento.y - 206) + "px", left: (posicaoElemento.x + 212) + "px", visibility: "visible"});
			var buscarPalpitesCallback = function(palpites) {
				var getters = [
					function(data) { return data.nomeParticipante; },
					function(data) { return data.representacaoPalpite; },
					function(data) { return data.pontos; }
				];
                var cellIndex = 0;
				var centeredTd = function(options) {
					var td = document.createElement("td");
					if (cellIndex == 1 || cellIndex == 2) {
						td.align= "center";
					}
                    cellIndex++;
					return td;
				};
                var rowCreator = function(options) {
                    var row = document.createElement("tr");
                    cellIndex = 0;
                    return row;
                }
				DWRUtil.addRows("balao_table_palpites", palpites, getters, {cellCreator: centeredTd, rowCreator: rowCreator});
				Element.setStyle($("loading_span_palpites"), {visibility: "hidden;"});
			}
			Element.setStyle($("loading_span_palpites"), {visibility: "visible"});
			ParticipanteAction.buscarPalpitesDoJogo(idJogo, {callback: buscarPalpitesCallback});
		}
	}

	function atualizarPalpite() {
		var loadingMsg = $("loading_span");
		Element.setStyle(loadingMsg, {visibility: "visible"});
        var fadeMsgFunc = function() {
            Element.setStyle($("span_retorno"), {visibility: "hidden"});
        }

		var callbackFunc = function() {
			Element.setStyle(loadingMsg, {visibility: "hidden"});
            DWRUtil.setValue("span_msg_retorno", "<fmt:message key='match.tip.status.ok' />");
            $("img_msg_retorno").src = "${base}/img/information.gif";
            Element.setStyle($("span_retorno"), {visibility: "visible"});
            window.setTimeout(fadeMsgFunc, 5000);
		}

        var errorCallbackFunc = function() {
            Element.setStyle(loadingMsg, {visibility: "hidden"});
            DWRUtil.setValue("span_msg_retorno", "<fmt:message key='match.tip.status.error' />");
            $("img_msg_retorno").src = "${base}/img/error.gif";
            Element.setStyle($("span_retorno"), {visibility: "visible"});
            window.setTimeout(fadeMsgFunc, 5000);
        }
        
        if (DWRUtil.getValue("palpite_gols_eq_1") == "") {
            DWRUtil.setValue("palpite_gols_eq_1", "0")
        }

        if (DWRUtil.getValue("palpite_gols_eq_2") == "") {
            DWRUtil.setValue("palpite_gols_eq_2", "0")
        }

		ParticipanteAction.atualizarPalpite(
			idJogoSelecionado, DWRUtil.getValue("palpite_gols_eq_1"),
			DWRUtil.getValue("palpite_gols_eq_2"),
            {callback: callbackFunc, errorHandler: errorCallbackFunc});
	}

	function fecharIconeMouseOver(img) {
		img.src = "${base}/img/fechar_hover.gif";
	}
	
	function fecharIconeMouseOut(img) {
		img.src = "${base}/img/fechar.gif";
	}
	
	function fecharBalao() {
		Element.setStyle($("balao_palpite"), {visibility: "hidden"});
		Element.setStyle($("balao_palpites"), {visibility: "hidden"});
		Element.setStyle($("loading_span"), {visibility: "hidden"});
		Element.setStyle($("loading_span_palpites"), {visibility: "hidden"});
        Element.setStyle($("span_retorno"), {visibility: "hidden"});
	}
	
	function mostrarMeusPalpites() {
		
		Effect.Appear($("todos_palpites_div"));
		buscarMeusPalpites();
	}
	
	var loadingPalpitesHtml = "<img alt=\"...\" src=\"${base}/img/loading.gif\" /><fmt:message key='general.loading' />";
	var fecharPalpitesHtml = "<a href=\"javascript:fecharMeusPalpites();\"><fmt:message key='match.tip.close' /></a>";
	
	function fecharMeusPalpites() {
		Effect.Fade($("todos_palpites_div"));
	}
	
	function buscarMeusPalpites() {
		DWRUtil.removeAllRows("todos_palpites_table");
		DWRUtil.setValue("todos_palpites_loading", loadingPalpitesHtml);
		var callBackFunc = function(palpites) {
			var getters = [
				function(data) { return data.dataDoJogo; },
				function(data) { return data.horaDoJogo; },
				function(data) { return data.paisEquipe1 + " X " + data.paisEquipe2; },
				function(data) { return data.golsEquipe1 + " X " + data.golsEquipe2; }
			];
			var centeredTd = function(options) {
				var td = document.createElement("td");
				td.align= "center";
				return td;
			};
            var rowIndex = 0;
			var alternateCollorRowCreator = function(options) {
				var row = document.createElement("tr");
				if (rowIndex % 2 == 0) {
					row.className = "impar";
				} else {
					row.className = "par";
				}
                rowIndex++;
				return row;
			};
			DWRUtil.addRows("todos_palpites_table", palpites, getters, {cellCreator: centeredTd, rowCreator: alternateCollorRowCreator});
			DWRUtil.setValue("todos_palpites_loading", fecharPalpitesHtml);
		};
		var errorCallBackFunc = function(exMsg, ex) {
			DWRUtil.setValue("todos_palpites_loading", fecharPalpitesHtml);
			alert(exMsg);
		};
		ParticipanteAction.buscarMeusPalpites({callback: callBackFunc, errorHandler: errorCallBackFunc});
		
	}
</script>
</authz:authorize>
<!--
<embed src="${base}/fx/popup.wav" id="popup_show_sound" autoplay="false" autostart="false" loop="false" hidden="true"></embed>
-->
<div id="balao_palpite">
	<div class="balao_top">
		<p>
			<span id="loading_span" style="visibility: hidden; padding-right: 60px;">
				<img alt="" src="${base}/img/loading.gif" style="vertical-align: top;" /> <fmt:message key="general.loading" />
			</span>
			<img alt="Fechar" src="${base}/img/fechar.gif"
				onclick="fecharBalao();" onmouseover="fecharIconeMouseOver(this);"
				onmouseout="fecharIconeMouseOut(this);" />
		</p>
	</div>
	<div class="balao_middle" style="height: 32px;">
		<div style="height: 16px; text-align: center;" id="balao_mensagem_retorno">
			<span id="span_retorno" style="visibility: hidden;"><img alt="" src="" id="img_msg_retorno" style="vertical-align: top;" /><span id="span_msg_retorno"></span></span>
		</div>
		<div style="text-align: center;">
			<label for="palpite_gols_eq_1"><fmt:message key="match.tip.self" /></label>
			<input type="text" name="palpiteGolsEq1" id="palpite_gols_eq_1" class="text" size="2" maxlength="2" style="text-align: center;" />
			<span>X</span>
			<input type="text" name="palpiteGolsEq2" id="palpite_gols_eq_2" class="text" size="2" maxlength="2" style="text-align: center;" />
			<fmt:message var="palpiteSubmitLabel" key="match.tip.confirm" />
			<input type="button" class="button" name="confirmarPalpite" id="confirmar_palpite_button" value="${palpiteSubmitLabel}" onclick="atualizarPalpite();" />
		</div>
	</div>
	<div class="balao_bottom"></div>
</div>

<div id="balao_palpites">
	<div class="balao_top">
		<p>
			<span id="loading_span_palpites" style="visibility: hidden; padding-right: 60px;">
				<img alt="" src="${base}/img/loading.gif" style="vertical-align: top;" /> <fmt:message key="general.loading" />
			</span>
			<img alt="Fechar" src="${base}/img/fechar.gif"
				onclick="fecharBalao();" onmouseover="fecharIconeMouseOver(this);"
				onmouseout="fecharIconeMouseOut(this);" />
		</p>
	</div>
	<div class="balao_middle" style="height: 122px;">
		<div style="overflow: scroll; width: 250px; margin: 0 auto; height: 112px;">
		<table cellspacing="1" cellpadding="2" align="center">
			<thead>
				<tr>
					<th><fmt:message key="member" /></th>
					<th><fmt:message key="match.tip" /></th>
					<th><fmt:message key="match.tip.points" /></th>
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
	<opendev:portlet id="filtro_jogos" title="Filtro de Busca" icon="/img/view.png" style="width: 600px; margin: 0 auto;">
		<c:url var="aplicarFiltroJogosActionURL" value="/seguro/palpites.action" />
		<form action="${aplicarFiltroJogosActionURL}" method="get">
        <input type="hidden" name="usarFiltro" value="true" />
		<div style="padding-top: 10px; padding-bottom: 10px;">
		<table class="form" cellspacing="0" cellpadding="2" style="width:100%; margin-top: 10px; margin-bottom: 10px;">
			<tr>
				<td class="label"><label for="filtro_time"><fmt:message key="filter.dates" /></label></td>
				<td class="widget">
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
				<td class="widget">
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
				<td class="widget">
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
		<div class="footer">
			<fmt:message var="filterSubmitLabel" key="filter.submit" />
			<input type="submit" name="submit" class="button" value="${filterSubmitLabel}" />
		</div>
		</form>
	</opendev:portlet>
	<div style="height: 20px;"></div>
    <div>
    	<p>
    		<img alt="" src="${base}/img/triang_yellow.png" style="vertical-align: top;" />
    		<fmt:message key="match.tip.showall">
    			<fmt:param value="javascript:mostrarMeusPalpites();"></fmt:param>
    		</fmt:message>
    	</p>
    </div>
    <div id="todos_palpites_div" style="display: none; width: 600px; margin: 0 auto; background-color: white; border: 1px dashed #283F08;">
    	<div style="margin: 4px;">
    		<div id="todos_palpites_loading" style="position: relative; top: 0; right: 0; text-align: right;">
    			<a href="javascript:fecharMeusPalpites();"><fmt:message key="match.tip.close" /></a>
    		</div>
    		<p><fmt:message key="match.tip.now" /> <img alt="" src="${base}/img/refresh.png" style="vertical-align: top; cursor: pointer;" onclick="buscarMeusPalpites();" /></p>
    		<table align="center" width="96%;" cellpadding="2" cellspacing="1" class="conteudo">
    			<thead>
    				<tr>
    					<th><fmt:message key="match.tip.date" /></th>
    					<th><fmt:message key="match.tip.hour" /></th>
    					<th><fmt:message key="match.tip.teams" /></th>
    					<th><fmt:message key="match.tip.mine" /></th>
    				</tr>
    			</thead>
    			<tbody id="todos_palpites_table">
    			</tbody>
    		</table>
    		<div style="height: 20px;"></div>
    	</div>
    </div>
    <div style="height: 10px;"></div>
    <div id="palpites_info" class="legenda" style="width: 600px; margin: 0 auto;">
        <p><img alt="" src="${base}/img/information.gif" style="vertical-align: top;" />
            <fmt:message key="match.tip.help" />
        </p>
    </div>
    <div style="height: 10px;"></div>
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
		<div style="height: 20px;"></div>
	</c:if>
	<c:if test="${empty dataJogo or dataJogo ne jogo.data}">
		<fmt:formatDate var="dataJogoFormatada" value="${jogo.data}" pattern="dd/MM/yyyy" />
		<div id="jogos_${dataJogoFormatada}_portlet" class="portlet" style="width: 600px; margin: 0 auto;">
			<div class="title" style="color: #283F08; text-align: center; background-image: url('${base}/img/fundo_cinza.png');">
				<span><img alt="" src="${base}/img/arrow_down.png" onclick="collapseContainer('jogos_${dataJogoFormatada}_portlet', this)" style="vertical-align: top; margin-top: 2px; cursor: pointer;" /></span>
				<span>
					<fmt:message key="matchs.day">
						<fmt:param value="${dataJogoFormatada}" />
					</fmt:message>
				</span>
			</div>
			<div class="content" id="jogos_${dataJogoFormatada}_portlet_content" style="padding: 0px;">
				<table width="100%" cellspacing="1" cellpadding="2" class="conteudo" align="center">
					<thead>
						<tr>
							<th><fmt:message key="match.hour" /></th>
							<th><fmt:message key="match.where" /></th>
							<th><fmt:message key="match.group" /></th>
							<th colspan="3"><fmt:message key="match.teams" /></th>
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
					<authz:authorize ifAllGranted="geral">
					<tr class="${rowStyleClass}" id="jogoTr_${jogo.id}" onmouseover="destacarLinha(this, true);" onmouseout="destacarLinha(this, false);" onclick="mostrarPopupPalpite(this, ${jogo.id}, ${jogo.podeDarPalpite});">
					</authz:authorize>
					<authz:authorize ifAllGranted="restrito">
					<tr class="${rowStyleClass}" id="jogoTr_${jogo.id}">
					</authz:authorize>
				</c:when>
				<c:otherwise>
					<tr class="${rowStyleClass}" id="jogoTr_${jogo.id}">
				</c:otherwise>
			</c:choose>
						<td align="center" style="width: 10%;">${horaJogoFormatada}</td>
						<td style="width: 16%;">${jogo.local}</td>
						<td align="center" style="width: 10%;">${jogo.equipe1.grupo}</td>
						<td align="right" style="width: 30%;">
							<span>${jogo.equipe1.nomePais}</span>
							<span><img alt="" src="${base}/img/bandeiras/${jogo.equipe1.id}.gif" /></span>							
							<c:choose>
								<c:when test="${not telaPalpites}">
									<authz:authorize ifAllGranted="admin">
										<span><input type="text" name="golsEquipe1" id="golsEquipe1_tf_${jogo.id}" class="text" maxlength="2" size="2" value="${jogo.golsEquipe1}" style="font-weight: normal; text-align: center;" /></span>
									</authz:authorize>
								</c:when>
								<c:otherwise>
									<span style="padding-left: 5px; padding-right: 5px; text-align: center;">${jogo.golsEquipe1}</span>
								</c:otherwise>
							</c:choose>
						</td>
						<td align="center" style="width: 4%;">X</td>
						<td align="left" style="width: 30%;">
								<c:choose>
								<c:when test="${not telaPalpites}">
									<authz:authorize ifAllGranted="admin">
										<span><input type="text" name="golsEquipe2" id="golsEquipe2_tf_${jogo.id}" class="text" maxlength="2" size="2" value="${jogo.golsEquipe2}" style="font-weight: normal; text-align: center;" onblur="atualizarResultado(this);" /></span>
									</authz:authorize>
								</c:when>
								<c:otherwise>
									<span style="padding-left: 5px; padding-right: 5px; text-align: center;">${jogo.golsEquipe2}</span>
								</c:otherwise>
								</c:choose>
							<span><img alt="" src="${base}/img/bandeiras/${jogo.equipe2.id}.gif" /></span>
							<span>${jogo.equipe2.nomePais}</span>
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
<div style="height: 20px;"></div>
</div>