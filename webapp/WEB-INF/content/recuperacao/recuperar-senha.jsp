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

<fmt:message key="recuperacao.titulo" var="recuperacaoTitulo" />
<opendev:portlet id="loginportlet" icon="/img/lock.png" title="${recuperacaoTitulo}">
    <div class="inner">
        
        <s:if test="mensagemNeutra != null && mensagemNeutra != ''">
            <div class="info" role="status" aria-live="polite">
                <span>
                    <img alt="" src="${base}/img/information.gif" class="icon-inline-top" />
                </span>
                <span><s:property value="mensagemNeutra"/></span>
            </div>
            <div class="spacer-sm"></div>
        </s:if>

        <s:if test="otpEnviado">
                <p class="helper-text">
                    <fmt:message key="recuperacao.otp.enviado.descricao">Um código foi enviado para o seu e-mail.</fmt:message>
                </p>
                <c:url var="validarOtpURL" value="/validarOtpRecuperacao.action" />
                <form id="formRecuperacao" action="${validarOtpURL}" method="post">
                    <div class="form-grid">
                        <div class="form-row" style="display: none;">
                            <label for="rec_email_validacao"><fmt:message key="recuperacao.email.label" /></label>
                            <input id="rec_email_validacao" type="email" name="email" class="text" value="${email}" autocomplete="email" required />
                        </div>
                        <div class="form-row">
                            <label for="rec_otp"><fmt:message key="recuperacao.otp.label" /></label>
                            <input id="rec_otp" type="text" name="otp" class="text" inputmode="numeric" pattern="[0-9]{6}" maxlength="6" autofocus required />
                        </div>
                    </div>
                </form>
            </s:if>
            <s:else>
                <p class="helper-text">
                    <fmt:message key="recuperacao.descricao" />
                </p>
                <c:url var="enviarOtpURL" value="/enviarOtpRecuperacao.action" />
                <form id="formRecuperacao" action="${enviarOtpURL}" method="post">
                    <div class="form-grid">
                        <div class="form-row">
                            <label for="rec_email"><fmt:message key="recuperacao.email.label" /></label>
                            <input id="rec_email" type="email" name="email" class="text" value="${email}" autocomplete="email" autofocus required />
                        </div>
                    </div>
                </form>
            </s:else>

        <div class="form-links">
            <c:url var="loginURL" value="/login.action" />
            <a href="${loginURL}" class="link-secondary"><fmt:message key="recuperacao.voltar.login" /></a>
        </div>
    </div>
    
    <div class="footer">
        <s:if test="otpEnviado">
                <fmt:message var="validarOtpLabel" key="recuperacao.otp.validar" />
                <input type="submit" class="button" value="${validarOtpLabel}" onclick="document.getElementById('formRecuperacao').submit(); return false;" />
        </s:if>
        <s:else>
                <fmt:message var="enviarOtpLabel" key="recuperacao.otp.enviar" />
                <input type="submit" class="button" value="${enviarOtpLabel}" onclick="document.getElementById('formRecuperacao').submit(); return false;" />
        </s:else>
    </div>
</opendev:portlet>
