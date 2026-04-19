<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="opendev" uri="http://www.opendev.com.br/tld" %>
<%@taglib prefix="s" uri="/struts-tags" %>

<div class="spacer-xl"></div>

<s:if test="sucessoCadastro">
    <div class="alert">
        <span>
            <img alt="" src="${base}/img/information.gif" class="icon-inline-top" />
        </span>
        <span id="field_info">
            <fmt:message key="pwd.change.success" />
        </span>
        <div class="text-center mt-md">
            <c:url var="homeURL" value="/seguro/principal.action" />
            <a href="${homeURL}" class="button">
                <fmt:message key="menu.geral.principal" />
            </a>
        </div>
    </div>
</s:if>
<s:else>
    <opendev:mensagensErro nomeAtributo="errosInclusao" />

    <fmt:message key="pwd.change.title" var="pwdTitle" />
    <opendev:portlet id="pwd_portlet" icon="/img/lock.png" title="${pwdTitle}">
        <s:form id="formAlterarSenha" action="alterarSenha" namespace="/seguro" method="post" theme="simple">
            <div class="inner">
                <div class="text-center mb-md">
                    <fmt:message key="pwd.change.description" />
                </div>

                <div class="form-grid">
                    <div class="form-row">
                        <label for="pwd_current">
                            <fmt:message key="pwd.change.current" />
                        </label>
                        <s:password id="pwd_current" name="senhaAtual" cssClass="text" autocomplete="current-password" autofocus="true" theme="simple" />
                    </div>
                    <div class="form-row">
                        <label for="pwd_new">
                            <fmt:message key="pwd.change.new" />
                        </label>
                        <s:password id="pwd_new" name="novaSenha" cssClass="text" autocomplete="new-password" theme="simple" />
                    </div>
                    <div class="form-row">
                        <label for="pwd_confirm">
                            <fmt:message key="pwd.change.confirm" />
                        </label>
                        <s:password id="pwd_confirm" name="confirmarSenha" cssClass="text" autocomplete="new-password" theme="simple" />
                    </div>
                </div>

                <div class="form-links mt-md">
                    <c:url var="principalURL" value="/seguro/principal.action" />
                    <a href="${principalURL}" class="button-secondary">
                        <fmt:message key="menu.geral.principal" />
                    </a>
                </div>
            </div>

            <div class="footer">
                <fmt:message var="submitLabel" key="pwd.change.submit" />
                <s:submit cssClass="button" value="%{#attr.submitLabel}" theme="simple" />
            </div>
        </s:form>
    </opendev:portlet>
</s:else>
