<c:choose>
<c:when test="${not empty sucessoCadastro and sucessoCadastro}">
	<div style="height: 150px;"></div>
    <div style="border: 1px dashed #283F08; padding: 2px; background-color: #FFFFE1; width: 400px; margin: 0 auto;">
        <span>
            <img alt="" src="${base}/img/information.gif" style="vertical-align: top;" />
        </span>
        <span id="field_info">
            Pedido de cadastro enviado! Em alguns minutos você receberá um email confirmando o seu cadastro
            caso você esteja participando do bolão! Aguarde!
        </span>
        <div style="text-align: center">
            <c:url var="contextURL" value="/" />
            <a href="${contextURL}">Página principal</a>
        </div>
    </div>
    <opendev:isIE>
        <div style="height: 350px;"></div>
    </opendev:isIE>
</c:when>
<c:otherwise>
<script type="text/javascript">
    function atualizarSugestao(sug) {
        DWRUtil.setValue("field_info", sug);
    }    
</script>
<div style="height: 90px;"></div>
<opendev:mensagensErro nomeAtributo="errosInclusao" />
<opendev:portlet id="cadastro_portlet" icon="/img/cadastro.png" title="Cadastro" style="width: 600px; margin: 0px auto;">
<c:url var="cadastroActionURL" value="/cadastro.action" />
<form action="${cadastroActionURL}" method="post">
    <div class="inner">
        <table class="form" cellspacing="0" cellpadding="2" style="width:100%; margin-top: 10px; margin-bottom: 10px;">
            <tr>
                <td colspan="2" style="text-align: left;">
                    <div style="border: 1px dashed #283F08; padding: 2px; background-color: #FFFFE1;">
                    <span>
                        <img alt="" src="${base}/img/information.gif" style="vertical-align: top;" />
                    </span>
                    <span id="field_info"></span>
                    </div>
                    <div style="height: 12px;"></div>
                </td>
            </tr>
            <tr>
                <td class="label">
                    <label for="login_tf"><fmt:message key="signin.login" /></label>
                </td>
                <td class="widget">
                    <input id="login_tf" type="text" name="login" onblur="atualizarSugestao('');" onfocus="atualizarSugestao('Procure usar o mesmo login da rede do BaCen para facilitar a identificação de cada um.');" class="text" size="15" value="${tentativaInclusao.login}" />
                </td>
            </tr>
            <tr>
                <td class="label">
                    <label for="pwd_tf"><fmt:message key="signin.pwd" /></label>
                </td>
                <td class="widget">
                    <input id="pwd_tf" type="password" name="senha" onblur="atualizarSugestao('');" onfocus="atualizarSugestao('Não utilize uma senha de fácil dedução. Não utilize caracteres especiais (-, _, *, &, etc). Mínimo de 5 e máximo de 20 caracteres.');" class="text" size="20" />
                </td>
            </tr>
            <tr>
                <td class="label">
                    <label for="nome_tf"><fmt:message key="signin.name" /></label>
                </td>
                <td class="widget">
                    <input id="nome_tf" type="text" name="nome" onblur="atualizarSugestao('');" onfocus="atualizarSugestao('Informe pelo menos um sobrenome.');" class="text" size="30" value="${tentativaInclusao.nome}" />
                </td>
            </tr>
            <tr>
                <td class="label">
                    <label for="email_tf"><fmt:message key="signin.email" /></label>
                </td>
                <td class="widget">
                    <input id="email_tf" type="text" name="email" onblur="atualizarSugestao('');" onfocus="atualizarSugestao('Deve-se informar o e-mail do Banco Central.');" class="text" size="35" value="${tentativaInclusao.email}" />
                </td>
            </tr>
		</table>
	</div>
	<div class="footer">
		<fmt:message var="siginSubmitLabel" key="signin.submit" />
		<input type="submit" name="submit" class="button" value="${siginSubmitLabel}" />
	</div>
</form>
</opendev:portlet>
<opendev:isIE>
<div style="height: 250px;"></div>
</opendev:isIE>
</c:otherwise>
</c:choose>