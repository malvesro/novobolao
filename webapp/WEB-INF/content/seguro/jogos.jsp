<%@include file="/WEB-INF/content/template/menu.jspf" %>
<fmt:message key="match.tip.state.dirty" var="msgTipDirty" />
<fmt:message key="match.tip.state.saving" var="msgTipSaving" />
<fmt:message key="match.tip.state.saved" var="msgTipSaved" />
<fmt:message key="match.tip.state.error" var="msgTipError" />
<fmt:message key="match.tip.state.locked" var="msgTipLocked" />
<fmt:message key="admin.result.state.saving" var="msgAdminSaving" />
<fmt:message key="admin.result.state.dirty" var="msgAdminDirty" />
<fmt:message key="admin.result.state.saved" var="msgAdminSaved" />
<fmt:message key="admin.result.state.error" var="msgAdminError" />
<fmt:message key="match.tip.session.saved" var="msgSessionSaved" />
<fmt:message key="match.tip.session.error" var="msgSessionError" />
<fmt:message key="admin.result.session.saved" var="msgAdminSessionSaved" />
<fmt:message key="admin.result.session.error" var="msgAdminSessionError" />

<c:if test="${telaPalpites}">
	<%-- Barra de Progresso Sticky (Modernização UX 2026) --%>
	<%@include file="/WEB-INF/content/seguro/partials/palpite-progress-bar.jspf" %>
</c:if>

					<div id="jogos-page-wrapper"
						class="dashboard-section"
						data-msg-tip-dirty="${msgTipDirty}"
						data-msg-tip-saving="${msgTipSaving}"
						data-msg-tip-saved="${msgTipSaved}"
						data-msg-tip-error="${msgTipError}"
						data-msg-tip-locked="${msgTipLocked}"
						data-msg-admin-saving="${msgAdminSaving}"
						data-msg-admin-dirty="${msgAdminDirty}"
						data-msg-admin-saved="${msgAdminSaved}"
						data-msg-admin-error="${msgAdminError}"
						data-msg-session-saved="${msgSessionSaved}"
						data-msg-session-error="${msgSessionError}"
						data-msg-admin-session-saved="${msgAdminSessionSaved}"
						data-msg-admin-session-error="${msgAdminSessionError}">
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
								<div id="palpites_info" class="legenda tips-info">
									<p><img alt="" src="${base}/img/information.gif" class="icon-inline"
											aria-hidden="true" />
										<fmt:message key="match.tip.help" />
									</p>
								</div>
								<span class="spacer spacer-sm"></span>
							</c:if>

								<c:if test="${not usarFiltro and empty param.dataInicial}">
									<div class="info-banner performance-notice">
										<p>
											<img src="${base}/img/information.gif" class="icon-inline" alt="" />
										Exibindo carga inicial reduzida para melhor performance.
										<a href="${pageContext.request.contextPath}/seguro/palpites.action?usarFiltro=true" class="link-action">Ver Calendário Completo</a>
									</p>
								</div>
									<span class="spacer spacer-sm"></span>
								</c:if>

								<c:if test="${adminResultadoView}">
									<c:if test="${adminFiltroAteHojeAtivo}">
										<fmt:formatDate var="adminDataLimiteFormatada" value="${adminFiltroDataLimite}" pattern="dd/MM/yyyy" />
										<div class="info-banner performance-notice">
											<p>
												<img src="${base}/img/information.gif" class="icon-inline" alt="" />
												Exibindo por padrão os jogos do início da Copa até hoje (${adminDataLimiteFormatada}) para facilitar correções de resultados.
												<a href="${pageContext.request.contextPath}/admin/jogos.action?mostrarTodos=true" class="link-action">Ver todos os jogos</a>
											</p>
										</div>
										<span class="spacer spacer-sm"></span>
									</c:if>
									<c:if test="${adminMostrandoTodos}">
										<div class="info-banner performance-notice">
											<p>
												<img src="${base}/img/information.gif" class="icon-inline" alt="" />
												Exibindo todos os jogos.
												<a href="${pageContext.request.contextPath}/admin/jogos.action" class="link-action">Voltar para jogos até hoje</a>
											</p>
										</div>
										<span class="spacer spacer-sm"></span>
									</c:if>
								</c:if>

								<div id="jogos-global-status"
									class="jogos-global-status"
									role="status"
									aria-live="polite"
									aria-atomic="true"></div>

								<div id="jogos-lista-container">
									<%@include file="/WEB-INF/content/seguro/partials/jogos-lista-fragmento.jsp" %>
								</div>

							<%-- Removido loop antigo que renderizava todos os jogos de uma vez --%>
					<%-- Removido fechamento do form externo --%>
						<div class="sticky-header"></div>
						<span class="spacer spacer-sm"></span>
						</div>

						<script src="${pageContext.request.contextPath}/js/ux-helper.js" defer nonce="${cspNonce}"></script>
