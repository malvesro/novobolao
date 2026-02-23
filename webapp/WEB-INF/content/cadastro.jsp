<c:choose>
<c:when test="${not empty sucessoCadastro and sucessoCadastro}">
	<div class="spacer-xl"></div>
    <div class="alert">
        <span>
            <img alt="" src="${base}/img/information.gif" class="icon-inline-top" />
        </span>
        <span id="field_info">
            Pedido de cadastro enviado! Em alguns minutos você receberá um email confirmando o seu cadastro
            caso você esteja participando do bolão! Aguarde!
        </span>
        <div class="text-center">
            <c:url var="contextURL" value="/" />
            <a href="${contextURL}">Página principal</a>
        </div>
    </div>
</c:when>
<c:otherwise>
<script type="text/javascript">
    (function() {
        window.atualizarSugestao = function(mensagem) {
            var alvo = document.getElementById("field_info");
            if (!alvo) {
                return;
            }
            alvo.textContent = mensagem || "";
        };
    })();
</script>
<div class="spacer-lg"></div>
<opendev:mensagensErro nomeAtributo="errosInclusao" />
<opendev:portlet id="cadastro_portlet" icon="/img/cadastro.png" title="Cadastro">
<c:url var="cadastroActionURL" value="/cadastro.action" />
    <form action="${cadastroActionURL}" method="post">
    <div class="inner">
        <div class="form-grid form-grid--two-cols">
            <div class="form-row form-row--full">
                <div class="alert">
                <span>
                    <img alt="" src="${base}/img/information.gif" class="icon-inline-top" />
                </span>
                <span id="field_info"></span>
                </div>
            </div>
            <div class="form-row">
                <label for="login_tf"><fmt:message key="signin.login" /></label>
                <input id="login_tf" type="text" name="login" onblur="atualizarSugestao('');" onfocus="atualizarSugestao('Use 3 a 32 caracteres: letras, números, ponto, hífen ou underline.');" class="text" value="${tentativaInclusao.login}" />
            </div>
            <div class="form-row">
                <label for="pwd_tf"><fmt:message key="signin.pwd" /></label>
                <input id="pwd_tf" type="password" name="senha" onblur="atualizarSugestao('');" onfocus="atualizarSugestao('Use entre 8 e 64 caracteres, misture maiúsculas, minúsculas, números e símbolos seguros (ex.: !@#$%).');" class="text" />
            </div>
            <div class="form-row">
                <label for="nome_tf"><fmt:message key="signin.name" /></label>
                <input id="nome_tf" type="text" name="nome" onblur="atualizarSugestao('');" onfocus="atualizarSugestao('Informe nome completo sem HTML, com pelo menos um sobrenome.');" class="text" value="${tentativaInclusao.nome}" />
            </div>
            <div class="form-row">
                <label for="email_tf"><fmt:message key="signin.email" /></label>
                <input id="email_tf" type="text" name="email" onblur="atualizarSugestao('');" onfocus="atualizarSugestao('Informe seu e-mail pessoal válido (usuario@dominio).');" class="text" value="${tentativaInclusao.email}" />
            </div>
        </div>
    </div>
	<div class="footer">
		<fmt:message var="siginSubmitLabel" key="signin.submit" />
		<input type="submit" name="submit" class="button" value="${siginSubmitLabel}" />
	</div>
</form>
</opendev:portlet>

</c:otherwise>
</c:choose>
