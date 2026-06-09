<div class="spacer-xl"></div>
<s:if test="%{errosRecuperacao != null && !errosRecuperacao.isEmpty()}">
    <div style="height: 10px;"></div>
    <div class="legenda" style="background-color: #ffeaea; border: 1px dashed #d9534f; padding: 15px; margin-bottom: 20px; text-align: left; border-radius: 4px;">
        <s:iterator value="errosRecuperacao">
            <p style="color: #d9534f; margin: 0 0 5px 0;">
                <img alt="Erro" src="${base}/img/error.gif" style="vertical-align: top;" />
                <strong><s:property value="nomeDoCampo" />:</strong> <s:property value="mensagem" />
            </p>
        </s:iterator>
    </div>
    <div style="height: 10px;"></div>
</s:if>

<style>
    .helper-text {
        text-align: center;
        color: var(--color-text-muted);
        margin-bottom: 1.5rem;
    }
    .form-links {
        text-align: center;
        margin-top: 1rem;
    }
</style>

<fmt:message key="recuperacao.redefinir.titulo" var="redefinirTitulo" />
<opendev:portlet id="loginportlet" icon="/img/lock.png" title="${redefinirTitulo}">
    <div class="inner">
        <c:if test="${not empty mensagemResultado}">
            <div class="alert" role="status" aria-live="polite">
                <span>
                    <img alt="" src="${base}/img/information.gif" class="icon-inline-top" />
                </span>
                <span>${mensagemResultado}</span>
            </div>
            <div class="spacer-sm"></div>
        </c:if>

        <c:if test="${not empty mensagemNeutra}">
            <div class="info" role="status" aria-live="polite">
                <span>
                    <img alt="" src="${base}/img/information.gif" class="icon-inline-top" />
                </span>
                <span>${mensagemNeutra}</span>
            </div>
            <div class="spacer-sm"></div>
        </c:if>

        <p class="helper-text">
            <fmt:message key="recuperacao.redefinir.descricao" />
        </p>

        <c:url var="redefinirSenhaURL" value="/redefinirSenha.action" />
        <form id="formRedefinir" action="${redefinirSenhaURL}" method="post">
            <c:if test="${not empty _csrf}">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
            </c:if>
            <input type="hidden" name="email" value="${email}" />
            <input type="hidden" name="otp" value="${otp}" />
            <div class="form-grid">
                <div class="form-row">
                    <label for="rec_senha"><fmt:message key="recuperacao.senha.label" /></label>
                    <input id="rec_senha" type="password" name="novaSenha" class="text" autocomplete="new-password" autofocus required />
                </div>
                <div class="form-row">
                    <label for="rec_senha_confirma"><fmt:message key="recuperacao.senha.confirmar.label" /></label>
                    <input id="rec_senha_confirma" type="password" name="confirmarSenha" class="text" autocomplete="new-password" required />
                </div>
            </div>
        </form>

        <div class="form-links">
            <c:url var="loginURL" value="/login.action" />
            <a href="${loginURL}" class="link-secondary"><fmt:message key="recuperacao.voltar.login" /></a>
        </div>
    </div>
    
    <div class="footer">
        <fmt:message var="redefinirLabel" key="recuperacao.redefinir.acao" />
        <input type="submit" class="button" value="${redefinirLabel}" onclick="document.getElementById('formRedefinir').submit(); return false;" />
    </div>
</opendev:portlet>
