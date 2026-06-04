<div class="spacer-lg"></div>

<%-- Exibe erros de validação do código --%>
<opendev:mensagensErro nomeAtributo="errosValidacao" />

<opendev:portlet id="validacao_portlet" icon="/img/cadastro.png" title="Verificação de E-mail">
                    <c:choose>
                        <%-- Cenário: Limite de tentativas atingido --%>
                            <c:when test="${tentativasRestantes <= 0}">
                                <div class="inner">
                                    <div class="alert">
                                        <span>
                                            <img alt="" src="${base}/img/information.gif" class="icon-inline-top" />
                                        </span>
                                        <span id="field_info">
                                            <strong>Limite de tentativas excedido.</strong><br />
                                            Não foi possível validar o seu cadastro com o código informado.
                                            Deseja gerar um novo código ou corrigir os dados informados (como o e-mail)?
                                        </span>
                                    </div>
                                    <div class="spacer-md"></div>
                                    <div style="display: flex; gap: 10px; justify-content: center; flex-wrap: wrap;">
                                        <c:url var="reenviarURL" value="/reenviarCodigo.action" />
                                        <c:url var="corrigirURL" value="/cadastroForm.action" />
                                        <c:url var="homeURL" value="/index.action" />

                                        <a href="${reenviarURL}" class="button">Gerar Novo Código</a>
                                        <a href="${corrigirURL}" class="button">Corrigir Cadastro / E-mail</a>
                                        <a href="${homeURL}" class="button secondary">Voltar ao Início</a>
                                    </div>
                                </div>
                                <div class="footer"></div>
                            </c:when>

                            <%-- Cenário: Fluxo normal de validação --%>
                                <c:otherwise>
                                    <c:url var="validarActionURL" value="/validarCodigo.action" />
                                    <form action="${validarActionURL}" method="post">
                                        <div class="inner">
                                            <div class="alert">
                                                <span>
                                                    <img alt="" src="${base}/img/information.gif"
                                                        class="icon-inline-top" />
                                                </span>
                                                <span id="field_info">
                                                    Enviamos um código de verificação para o e-mail informado no
                                                    cadastro.
                                                    Por favor, insira o código de 6 caracteres abaixo.<br />
                                                    <strong>Tentativas restantes: ${tentativasRestantes}</strong>
                                                </span>
                                            </div>

                                            <div class="spacer-lg"></div>

                                            <div class="form-grid" style="grid-template-columns: 1fr;">
                                                <div class="form-row" style="text-align: center;">
                                                    <label for="codigo_tf"
                                                        style="display: block; margin-bottom: 15px;">Código de 6
                                                        caracteres:</label>
                                                    <input id="codigo_tf" type="text" name="codigo" class="text"
                                                        maxlength="6" autocomplete="off"
                                                        style="font-family: monospace; font-size: 2.5em; width: 250px; text-align: center; letter-spacing: 5px; text-transform: none; height: 60px;"
                                                        required />
                                                </div>
                                            </div>
                                        </div>
                                        <div class="footer">
                                            <input type="submit" class="button" value="Confirmar Cadastro" />
                                            <c:url var="homeURL" value="/index.action" />
                                            <a href="${homeURL}" class="button secondary"
                                                style="margin-left: 10px;">Cancelar</a>
                                        </div>
                                    </form>
                                </c:otherwise>
                    </c:choose>
                </opendev:portlet>