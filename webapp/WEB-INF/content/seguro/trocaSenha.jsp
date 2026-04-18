<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="opendev" uri="http://www.opendev.com.br/tld" %>
<%@taglib prefix="s" uri="/struts-tags" %>

<div class="spacer-xl"></div>

<c:choose>
    <c:when test="${not empty sucessoCadastro and sucessoCadastro}">
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
                    <fmt:message key="general.back" />
                </a>
            </div>
        </div>
    </c:when>
    <c:otherwise>
        <opendev:mensagensErro nomeAtributo="errosInclusao" />

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

                <div class="form-links mt-md">
                    <c:url var="principalURL" value="/seguro/principal.action" />
                    <a href="${principalURL}" class="link-secondary"><fmt:message key="general.back" /></a>
                </div>
            </div>

            <div class="footer">
                <fmt:message var="submitLabel" key="pwd.change.submit" />
                <input type="submit" class="button" value="${submitLabel}" onclick="document.getElementById('formAlterarSenha').submit(); return false;" />
            </div>
        </opendev:portlet>
    </c:otherwise>
</c:choose>
