<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="opendev" uri="http://www.opendev.com.br/tld" %>
<%@taglib prefix="s" uri="/struts-tags" %>

<div class="spacer-xl"></div>

<s:if test="hasActionErrors()">
    <div class="error" role="alert" aria-live="assertive">
        <s:iterator value="actionErrors">
            <p>
                <img alt="Erro" src="${base}/img/error.gif" class="icon-inline-top" />
                <s:property />
            </p>
        </s:iterator>
    </div>
    <div class="spacer-sm"></div>
</s:if>

<s:if test="hasActionMessages()">
    <div class="info" role="status" aria-live="polite">
        <s:iterator value="actionMessages">
            <p>
                <img alt="Sucesso" src="${base}/img/information.gif" class="icon-inline-top" />
                <s:property />
            </p>
        </s:iterator>
    </div>
    <div class="spacer-sm"></div>
</s:if>

<fmt:message key="pwd.change.title" var="pwdTitle" />
<opendev:portlet id="loginportlet" icon="/img/lock.png" title="${pwdTitle}">
    <div class="inner">
        <div class="text-center mb-md">
            <fmt:message key="pwd.change.description" />
        </div>

        <c:url var="alterarSenhaURL" value="/seguro/alterarSenha.action" />
        <form id="formAlterarSenha" action="${alterarSenhaURL}" method="post">
            <s:token />
            <div class="form-grid">
                <div class="form-row">
                    <label for="pwd_current">
                        <fmt:message key="pwd.change.current" />
                    </label>
                    <input id="pwd_current" type="password" name="senhaAtual" class="text" autocomplete="current-password" autofocus required />
                </div>
                <div class="form-row">
                    <label for="pwd_new">
                        <fmt:message key="pwd.change.new" />
                    </label>
                    <input id="pwd_new" type="password" name="novaSenha" class="text" autocomplete="new-password" required />
                </div>
                <div class="form-row">
                    <label for="pwd_confirm">
                        <fmt:message key="pwd.change.confirm" />
                    </label>
                    <input id="pwd_confirm" type="password" name="confirmarSenha" class="text" autocomplete="new-password" required />
                </div>
            </div>
        </form>
    </div>

    <div class="footer">
        <fmt:message var="submitLabel" key="pwd.change.submit" />
        <input type="submit" class="button" value="${submitLabel}" onclick="document.getElementById('formAlterarSenha').submit(); return false;" />
    </div>
</opendev:portlet>
