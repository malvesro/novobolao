<%@include file="/WEB-INF/content/template/menu.jspf" %>

<div class="dashboard-section">
    <c:set var="whatsappGroupUrl" value="https://chat.whatsapp.com/DHVbMfWS87TEeORUs6R8pK?s=sw&amp;p=a&amp;mlu=4" />
    <fmt:message key="home.whatsapp.title" var="homeWhatsappTitle" />
    <fmt:message key="home.whatsapp.description" var="homeWhatsappDescription" />
    <fmt:message key="home.whatsapp.cta" var="homeWhatsappCta" />
    <fmt:message key="home.whatsapp.qr.alt" var="homeWhatsappQrAlt" />
    <fmt:message key="home.whatsapp.qr.caption" var="homeWhatsappQrCaption" />
    <fmt:message key="home.whatsapp.qr.linkAria" var="homeWhatsappQrLinkAria" />
    <section class="whatsapp-invite-card" role="region" aria-labelledby="homeWhatsappTitle">
        <div class="whatsapp-invite-card__content">
            <h3 id="homeWhatsappTitle" class="whatsapp-invite-card__title">${homeWhatsappTitle}</h3>
            <p class="whatsapp-invite-card__description">${homeWhatsappDescription}</p>
            <a class="whatsapp-invite-card__cta"
               href="${whatsappGroupUrl}"
               target="_blank"
               rel="noopener noreferrer">
                ${homeWhatsappCta}
            </a>
        </div>
        <a class="whatsapp-invite-card__qr-link"
           href="${whatsappGroupUrl}"
           target="_blank"
           rel="noopener noreferrer"
           aria-label="${homeWhatsappQrLinkAria}">
            <img class="whatsapp-invite-card__qr-image"
                 src="${pageContext.request.contextPath}/img/qrcode.png"
                 alt="${homeWhatsappQrAlt}" />
            <span class="whatsapp-invite-card__qr-caption">${homeWhatsappQrCaption}</span>
        </a>
    </section>
    <span class="spacer-sm"></span>

    <c:if test="${empty jogosDeHoje}">
        <opendev:portlet id="jogos_hoje_vazio" title="Jogos de Hoje">
            <div class="info">
                <fmt:message key="match.none.today" />
            </div>
        </opendev:portlet>
    </c:if>

    <c:if test="${not empty jogosDeHoje}">
    <opendev:portlet id="jogos_hoje_portlet" title="Jogos de Hoje">
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
            <c:forEach var="jogo" items="${jogosDeHoje}" varStatus="loop">
        		<c:choose>
        			<c:when test="${jogo.equipe1.nomePais eq 'Brasil' or jogo.equipe2.nomePais eq 'Brasil'}">
        				<c:set var="rowStyleClass" value="brasil" />
        			</c:when>
        			<c:when test="${loop.index mod 2 eq 0}">
        				<c:set var="rowStyleClass" value="impar" />
        			</c:when>
        			<c:otherwise>
        				<c:set var="rowStyleClass" value="par" />
        			</c:otherwise>
        		</c:choose>
                <tr class="${rowStyleClass}" id="jogoTr_${jogo.id}">
                    <fmt:formatDate var="horaJogoFormatada" value="${jogo.hora}" pattern="HH:mm" />
					<td class="text-center">${horaJogoFormatada}</td>
					<td>${jogo.local}</td>
					<td class="text-center">
						<c:choose>
							<c:when test="${jogo.faseDeGrupos}">
								<c:choose>
									<c:when test="${not empty jogo.equipe1.grupo}">
										<fmt:message key="match.group" var="grupoLabel" />
										<span>${grupoLabel} ${jogo.equipe1.grupo}</span>
									</c:when>
									<c:otherwise>
										<span>${jogo.descricaoFase}</span>
									</c:otherwise>
								</c:choose>
							</c:when>
							<c:otherwise>
								<span>${jogo.descricaoFase}</span>
							</c:otherwise>
						</c:choose>
					</td>
					<td class="text-right">
						<div class="team-cell text-right">
							<span><c:out value="${jogo.equipe1.nomePais}" /></span>
								<c:choose>
									<c:when test="${not empty jogo.equipe1.bandeiraUrl}">
										<img class="flag-icon icon-inline" src="${pageContext.request.contextPath}${jogo.equipe1.bandeiraUrl}" alt="Bandeira de ${jogo.equipe1.nomePais}" width="24" height="18" loading="lazy" />
									</c:when>
									<c:when test="${not empty jogo.equipe1.emojiBandeira}">
										<span class="flag-icon icon-inline" role="img" aria-label="${jogo.equipe1.nomePais}">
											<c:out value="${jogo.equipe1.emojiBandeira}" />
										</span>
									</c:when>
									<c:otherwise>
										<span class="flag-icon flag-icon--fallback icon-inline" aria-hidden="true">
											<c:out value="${jogo.equipe1.siglaPais}" />
										</span>
									</c:otherwise>
								</c:choose>
							<span class="score-value"><c:out value="${jogo.golsEquipe1}" /></span>
						</div>
					</td>
						<td class="text-center">X</td>
						<td class="text-left">
							<div class="team-cell text-left">
								<span class="score-value"><c:out value="${jogo.golsEquipe2}" /></span>
								<c:choose>
									<c:when test="${not empty jogo.equipe2.bandeiraUrl}">
										<img class="flag-icon icon-inline" src="${pageContext.request.contextPath}${jogo.equipe2.bandeiraUrl}" alt="Bandeira de ${jogo.equipe2.nomePais}" width="24" height="18" loading="lazy" />
									</c:when>
									<c:when test="${not empty jogo.equipe2.emojiBandeira}">
										<span class="flag-icon icon-inline" role="img" aria-label="${jogo.equipe2.nomePais}">
											<c:out value="${jogo.equipe2.emojiBandeira}" />
										</span>
									</c:when>
									<c:otherwise>
										<span class="flag-icon flag-icon--fallback icon-inline" aria-hidden="true">
											<c:out value="${jogo.equipe2.siglaPais}" />
										</span>
									</c:otherwise>
								</c:choose>
								<span><c:out value="${jogo.equipe2.nomePais}" /></span>
							</div>
						</td>
					</tr>
            </c:forEach>
            </tbody>
        </table>
		</div>
    </opendev:portlet>
    <span class="spacer-sm"></span>
    </c:if>
    <opendev:portlet id="grafico_lideres_portlet" title="Lideranca">
        <fmt:message key="home.leaders.summary.title" var="leadersSummaryTitle" />
        <fmt:message key="home.leaders.summary.empty" var="leadersSummaryEmpty" />
        <fmt:message key="home.leaders.points.label" var="leadersPointsLabel" />
        <fmt:message key="home.leaders.summary.chartAlt" var="leadersChartAlt" />
        <fmt:message key="home.leaders.summary.ctaFullRanking" var="leadersCtaFullRanking" />
        <c:url var="graficoLideresUrl" value="/seguro/graficoLiderancaImagem.action" />
        <c:url var="rankingActionUrl" value="/seguro/ranking.action" />
        <div class="leaders-summary" role="region" aria-labelledby="leadersSummaryTitle">
            <h3 id="leadersSummaryTitle" class="leaders-summary__title">${leadersSummaryTitle}</h3>
            <p class="leaders-summary__note"><fmt:message key="home.leaders.summary.medalRule" /></p>
            <c:if test="${liderancaDesempateAplicado}">
                <p class="leaders-summary__note"><fmt:message key="home.leaders.summary.tieApplied" /></p>
            </c:if>
            <c:if test="${liderancaEmpatadosMesmoPontosRestantes gt 0}">
                <p class="leaders-summary__note">
                    <fmt:message key="home.leaders.summary.samePointsMore">
                        <fmt:param value="${liderancaEmpatadosMesmoPontosRestantes}" />
                        <fmt:param value="${lideresResumo[0].pontuacaoTotal.pontuacao}" />
                    </fmt:message>
                </p>
                <p class="leaders-summary__cta-wrap">
                    <a class="leaders-summary__cta" href="${rankingActionUrl}">
                        ${leadersCtaFullRanking}
                    </a>
                </p>
            </c:if>
            <c:choose>
                <c:when test="${empty lideresResumo}">
                    <p class="leaders-summary__empty">${leadersSummaryEmpty}</p>
                </c:when>
                <c:otherwise>
                    <ol class="leaders-summary__list">
                        <c:forEach var="lider" items="${lideresResumo}" varStatus="loop">
                            <c:choose>
                                <c:when test="${loop.count eq 1}">
                                    <fmt:message key="home.leaders.medal.gold" var="medalLabel" />
                                    <c:set var="medalClass" value="leaders-summary__medal--gold" />
                                </c:when>
                                <c:when test="${loop.count eq 2}">
                                    <fmt:message key="home.leaders.medal.silver" var="medalLabel" />
                                    <c:set var="medalClass" value="leaders-summary__medal--silver" />
                                </c:when>
                                <c:otherwise>
                                    <fmt:message key="home.leaders.medal.bronze" var="medalLabel" />
                                    <c:set var="medalClass" value="leaders-summary__medal--bronze" />
                                </c:otherwise>
                            </c:choose>
                            <fmt:message key="home.leaders.summary.itemLabel" var="leadersItemAria">
                                <fmt:param value="${loop.count}" />
                                <fmt:param value="${lider.pontuacaoTotal.nomeParticipante}" />
                                <fmt:param value="${lider.pontuacaoTotal.pontuacao}" />
                                <fmt:param value="${medalLabel}" />
                            </fmt:message>
                            <li class="leaders-summary__item" aria-label="${leadersItemAria}">
                                <span class="leaders-summary__position">
                                    <svg class="leaders-summary__position-icon ${medalClass}" viewBox="0 0 24 24" width="12" height="12" preserveAspectRatio="xMidYMid meet" focusable="false" aria-hidden="true">
                                        <path class="leaders-summary__medal-ribbon" d="M7 2h4v6l-2-1.4L7 8z"></path>
                                        <path class="leaders-summary__medal-ribbon" d="M13 2h4v6l-2-1.4L13 8z"></path>
                                        <circle class="leaders-summary__medal-disc" cx="12" cy="14" r="7"></circle>
                                        <path class="leaders-summary__medal-star" d="M12 9.8l1.25 2.54 2.8.41-2.03 1.98.48 2.79L12 16.2l-2.5 1.31.48-2.79-2.03-1.98 2.8-.41L12 9.8z"></path>
                                    </svg>
                                    ${loop.count}º
                                </span>
                                <span class="leaders-summary__name"><c:out value="${lider.pontuacaoTotal.nomeParticipante}" /></span>
                                <span class="leaders-summary__points">
                                    <c:out value="${lider.pontuacaoTotal.pontuacao}" /> ${leadersPointsLabel}
                                </span>
                            </li>
                        </c:forEach>
                    </ol>
                </c:otherwise>
            </c:choose>
        </div>
        <div class="chart-wrapper">
            <img src="${graficoLideresUrl}" alt="${leadersChartAlt}" />
        </div>
    </opendev:portlet>
</div>
