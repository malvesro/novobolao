<%@include file="/WEB-INF/content/template/menu.jspf" %>
	<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
		<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
			<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
				<%@taglib prefix="opendev" uri="http://www.opendev.com.br/tld" %>

					<c:if test="${telaPalpites}">
						<%-- Barra de Progresso Sticky (Modernização UX 2026) --%>
							<%@include file="/WEB-INF/content/seguro/partials/palpite-progress-bar.jspf" %>
					</c:if>

					<div id="jogos-page-wrapper" class="dashboard-section">
						<%-- Removido form externo que causava aninhamento inválido --%>

							<c:set var="dataJogo" />
							<c:set var="rowIndex" value="0" />


							<c:if test="${telaPalpites}">
								<div class="match-filter-portlet">
									<opendev:portlet id="filtro_jogos" title="Filtro de Busca" icon="/img/view.png">
										<c:url var="aplicarFiltroJogosActionURL" value="/seguro/palpites.action" />
										<form action="${aplicarFiltroJogosActionURL}" method="get">
											<input type="hidden" name="usarFiltro" value="true" />
											<div class="match-filter">
												<table class="match-filter__table" role="presentation">
													<tr>
														<td class="label"><label for="data_inicial_select">
																<fmt:message key="filter.dates" />
															</label></td>
														<td class="widget match-filter__row">
															<select name="dataInicial" id="data_inicial_select">
																<option value="">
																	<fmt:message key="filter.fase.0" />
																</option>
																<c:forTokens var="data" delims=","
																	items="${initParam.datas}">
																	<c:choose>
																		<c:when
																			test="${not empty filtro and filtro.dataInicialFormatada eq data}">
																			<option value="${data}" selected="selected">
																				${data}</option>
																		</c:when>
																		<c:otherwise>
																			<option value="${data}">${data}</option>
																		</c:otherwise>
																	</c:choose>
																</c:forTokens>
															</select>
															<fmt:message key="filter.dates.and" />
															<select name="dataFinal" id="data_final_select">
																<option value="">
																	<fmt:message key="filter.fase.0" />
																</option>
																<c:forTokens var="data" delims=","
																	items="${initParam.datas}">
																	<c:choose>
																		<c:when
																			test="${not empty filtro and filtro.dataFinalFormatada eq data}">
																			<option value="${data}" selected="selected">
																				${data}</option>
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
														<td class="label"><label for="filtro_equipe">
																<fmt:message key="filter.team" />
															</label></td>
														<td class="widget">
															<select name="filtroEquipe" id="filtro_equipe">
																<option value="">
																	<fmt:message key="filter.fase.0" />
																</option>
																<c:forEach var="equipe" items="${equipes}">
																	<c:if
																		test="${not empty grupoAnterior and grupoAnterior ne equipe.grupo}">
																		</optgroup>
																	</c:if>
																	<c:if
																		test="${empty grupoAnterior or (grupoAnterior ne equipe.grupo)}">
																		<optgroup label="Grupo ${equipe.grupo}">
																	</c:if>
																	<c:choose>
																		<c:when
																			test="${not empty filtro and filtro.idEquipe eq equipe.id}">
																			<option value="${equipe.id}"
																				selected="selected">
																				${equipe.nomePais}</option>
																		</c:when>
																		<c:otherwise>
																			<option value="${equipe.id}">
																				${equipe.nomePais}</option>
																		</c:otherwise>
																	</c:choose>
																	<c:set var="grupoAnterior"
																		value="${equipe.grupo}" />
																</c:forEach>
															</select>
														</td>
													</tr>
													<tr>
														<td class="label"><label for="filtro_grupo">
																<fmt:message key="filter.group" />
															</label></td>
														<td class="widget">
															<select name="filtroGrupo" id="filtro_grupo">
																<option value="">
																	<fmt:message key="filter.group.0" />
																</option>
																<c:set var="grupoAnterior" value="" />
																<c:forEach var="equipeGrupo" items="${equipes}">
																	<c:if
																		test="${empty grupoAnterior or grupoAnterior ne equipeGrupo.grupo}">
																		<c:set var="grupoAnterior"
																			value="${equipeGrupo.grupo}" />
																		<c:choose>
																			<c:when
																				test="${not empty filtro and filtro.grupo eq grupoAnterior}">
																				<option value="${grupoAnterior}"
																					selected="selected">
																					${grupoAnterior}</option>
																			</c:when>
																			<c:otherwise>
																				<option value="${grupoAnterior}">
																					${grupoAnterior}</option>
																			</c:otherwise>
																		</c:choose>
																	</c:if>
																</c:forEach>
															</select>
														</td>
													</tr>
													<tr>
														<td class="label"><label for="filtro_fase">
																<fmt:message key="filter.fase" />
														</td>
														<td class="widget">
															<select name="filtroFase" id="filtro_fase">
																<option value="">
																	<fmt:message key="filter.fase.0" />
																</option>
																<c:forTokens var="fase" items="11,12,13,16,8,4,2,3,1"
																	delims=",">
																	<c:choose>
																		<c:when
																			test="${not empty filtro and filtro.fase eq fase}">
																			<option value="${fase}" selected="selected">
																				<fmt:message
																					key="filter.fase.${fase}" />
																			</option>
																		</c:when>
																		<c:otherwise>
																			<option value="${fase}">
																				<fmt:message
																					key="filter.fase.${fase}" />
																			</option>
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
																<c:when
																	test="${not empty filtro and filtro.soSemPalpite}">
																	<input type="checkbox" id="filtro_sem_palpite_cb"
																		name="filtroSemPalpite" value="true"
																		checked="checked" />
																</c:when>
																<c:otherwise>
																	<input type="checkbox" id="filtro_sem_palpite_cb"
																		name="filtroSemPalpite" value="true" />
																</c:otherwise>
															</c:choose>
															<label for="filtro_sem_palpite_cb">
																<fmt:message key="filter.withouttip" />
															</label>
														</td>
													</tr>
													<tr>
														<td><br /></td>
														<td class="widget match-filter__row">
															<c:choose>
																<c:when
																	test="${not empty filtro and filtro.soJogosQueNaoOcorreram}">
																	<input type="checkbox"
																		id="filtro_jogos_ocorreram_cb"
																		name="filtroJogosNaoOcorreram" value="true"
																		checked="checked" />
																</c:when>
																<c:otherwise>
																	<input type="checkbox"
																		id="filtro_jogos_ocorreram_cb"
																		name="filtroJogosNaoOcorreram" value="true" />
																</c:otherwise>
															</c:choose>
															<label for="filtro_jogos_ocorreram_cb">
																<fmt:message key="filter.notplayed" />
															</label>
														</td>
													</tr>
												</table>
											</div>
											<div class="match-filter__actions">
												<fmt:message var="filterSubmitLabel" key="filter.submit" />
												<input type="submit" name="submit" class="button"
													value="${filterSubmitLabel}" />
											</div>
										</form>
									</opendev:portlet>
								</div>
								<span class="spacer spacer-sm"></span>
								<details class="portlet collapsible-portlet" hx-get="${meusPalpitesUrl}"
									hx-trigger="toggle once" hx-target="#todos_palpites_table" hx-swap="innerHTML">
									<summary class="collapsible-portlet__header"
										style="cursor: pointer; font-weight: bold; padding: 0.6rem; border-bottom: 1px solid var(--color-border); list-style: none;">
										<img alt="" src="${base}/img/triang_yellow.png" class="icon-inline" />
										Ver meus palpites
									</summary>
									<div class="portlet-content tips-panel__body" style="padding: 1rem;">
										<div class="tips-panel__footer" style="padding-bottom: 0.5rem;">
											<p style="margin: 0; font-weight: bold;">
												<fmt:message key="match.tip.now" />
											</p>
											<button type="button" class="icon-button button-ghost"
												hx-get="${meusPalpitesUrl}" hx-target="#todos_palpites_table"
												hx-swap="innerHTML" title="Recarregar meus palpites">
												<img alt="Recarregar" src="${base}/img/refresh.png"
													class="icon-inline" />
											</button>
										</div>
										<div class="tips-panel__scroll" style="overflow-x: auto;">
											<table class="table tips-panel__table">
												<thead>
													<tr>
														<th scope="col">
															<fmt:message key="match.tip.date" />
														</th>
														<th scope="col">
															<fmt:message key="match.tip.hour" />
														</th>
														<th scope="col">
															<fmt:message key="match.tip.teams" />
														</th>
														<th scope="col" style="text-align: center;">
															<fmt:message key="match.tip.mine" />
														</th>
													</tr>
												</thead>
												<tbody id="todos_palpites_table">
													<tr>
														<td colspan="4" class="text-center" style="padding: 1rem;">
															<fmt:message key="general.loading" />
														</td>
													</tr>
												</tbody>
											</table>
										</div>
									</div>
								</details>
								<span class="spacer spacer-sm"></span>
								<span class="spacer spacer-sm"></span>
								<div id="palpites_info" class="legenda tips-info">
									<p><img alt="" src="${base}/img/information.gif" class="icon-inline"
											aria-hidden="true" />
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
									</table>
					</div>
					</div>
					</div>
					<span class="spacer spacer-sm"></span>
					</c:if>
					<c:if test="${empty dataJogo or dataJogo ne jogo.data}">
						<div id="jogos_${dataJogoFormatada}_portlet" class="portlet collapsible-portlet">
							<div class="title collapsible-portlet__header">
								<img alt="Alternar exibição do filtro" src="${base}/img/arrow_down.png"
									class="collapse-toggle icon-inline-top icon-button"
									data-target="jogos_${dataJogoFormatada}_portlet" />
								<fmt:message key="matchs.day">
									<fmt:param value="${dataJogoFormatada}" />
								</fmt:message>
							</div>
							<div class="content collapsible-portlet__content"
								id="jogos_${dataJogoFormatada}_portlet_content">
								<div class="table-responsive">
									<table class="table conteudo match-table">
										<thead>
											<tr>
												<th scope="col">
													<fmt:message key="match.hour" />
												</th>
												<th scope="col">
													<fmt:message key="match.where" />
												</th>
												<th scope="col">
													<fmt:message key="match.group" />
												</th>
												<th scope="colgroup" colspan="3">
													<fmt:message key="match.teams" />
												</th>
												<c:if test="${telaPalpites}">
													<th scope="col">
														<fmt:message key="match.tip.mine" />
													</th>
													<th scope="col"></th>
												</c:if>
												<sec:authorize access="hasRole('ADMIN')">
													<th scope="col" style="width: 40px;"></th>
												</sec:authorize>
											</tr>
										</thead>
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
							<c:set var="podeRegistrarPalpite" value="false" />
							<sec:authorize access="hasAnyRole('USER', 'ADMIN')">
								<c:set var="podeRegistrarPalpite" value="true" />
							</sec:authorize>
							<c:set var="palpitePermitido" value="${podeRegistrarPalpite and jogo.podeDarPalpite}" />
							<c:set var="palpiteStatus" value="locked" />
							<c:if test="${not empty palpiteUsuario}">
								<c:set var="palpiteStatus" value="registered" />
							</c:if>
							<c:if test="${empty palpiteUsuario and palpitePermitido}">
								<c:set var="palpiteStatus" value="pending" />
							</c:if>
							<c:set var="palpiteBloqueioMotivo" value="" />
							<c:choose>
								<c:when test="${palpitePermitido}">
									<c:set var="palpiteBloqueioMotivo" value="" />
								</c:when>
								<c:when test="${not podeRegistrarPalpite}">
									<c:set var="palpiteBloqueioMotivo" value="roleMissing" />
								</c:when>
								<c:when test="${not jogo.podeDarPalpite}">
									<c:set var="palpiteBloqueioMotivo" value="timeWindow" />
								</c:when>
								<c:otherwise>
									<c:set var="palpiteBloqueioMotivo" value="unknown" />
								</c:otherwise>
							</c:choose>
							<c:set var="palpiteStatusKey">match.tip.status.${palpiteStatus}</c:set>
							<fmt:message key="${palpiteStatusKey}" var="palpiteStatusLabel" />
							<fmt:message key="match.tip.none" var="palpiteSemRegistro" />

							<tbody>
								<%@include file="/WEB-INF/content/seguro/partials/match-row.jspf" %>
							</tbody>
						</c:when>
						<c:otherwise>
							<tbody>
								<%@include file="/WEB-INF/content/admin/partials/admin-match-row.jsp" %>
							</tbody>
						</c:otherwise>
					</c:choose>
					<c:set var="dataJogo" value="${jogo.data}" />
					<c:if test="${loop.count eq fn:length(jogos)}">
						</table>
						</div>
						</div>
						</div>
					</c:if>
					<c:set var="rowIndex" value="${rowIndex + 1}" />
					</c:forEach>
					<%-- Removido fechamento do form externo --%>
						<div class="sticky-header"></div>
						<span class="spacer spacer-sm"></span>
						</div>

						<script src="${pageContext.request.contextPath}/js/ux-helper.js" defer></script>