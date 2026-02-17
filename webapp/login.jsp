<div style="height: 90px;"></div>
<c:if test="${not empty param.status and param.status eq 'invalido'}">
	<span id="login_error" class="error">
		<span><img alt="Erro" src="${base}/img/error.gif" style="vertical-align: top;" /></span>
		<span>
			<fmt:message key="login.error" />
		</span>
	</span>
	<script type="text/javascript">
		var errorTimeout = 0;
		// Pulse effect using jQuery
		$j("#login_error").fadeOut(200).fadeIn(200).fadeOut(200).fadeIn(200);
		var fadeFunc = function () {
			$j("#login_error").fadeOut(1000);
			window.clearTimeout(errorTimeout);
		};
		errorTimeout = window.setTimeout(fadeFunc, 6000);
	</script>
	<br />
</c:if>
<opendev:portlet id="loginportlet" icon="/img/lock.png" title="Login" style="width: 350px; margin: 0px auto;">
	<form action="j_security_check" method="post">
		<div class="inner">
			<table class="form" cellspacing="0" cellpadding="2"
				style="width:100%; margin-top: 10px; margin-bottom: 10px;">
				<tr>
					<td class="label"><label for="login_username">
							<fmt:message key="login.username" />
						</label></td>
					<td class="widget"><input id="login_username" type="text" name="j_username" class="text" /></td>
				</tr>
				<tr>
					<td class="label"><label for="login_pwd">
							<fmt:message key="login.pwd" />
					</td>
					<td class="widget"><input id="login_pwd" type="password" name="j_password" class="text" /></td>
				</tr>
				<tr>
					<td colspan="2"><br /></td>
				</tr>
				<tr>
					<td style="text-align: center;" colspan="2">
						<c:url var="cadastroFormURL" value="/cadastro.jsp" />
						<a href="${cadastroFormURL}">
							<fmt:message key="login.signin" />
						</a>
					</td>
				</tr>
			</table>
		</div>
		<div class="footer">
			<fmt:message var="loginSubmitLabel" key="login.go" />
			<input type="submit" name="submit" class="button" value="${loginSubmitLabel}" />
		</div>
	</form>
</opendev:portlet>
<opendev:isIE>
	<div style="height: 250px;"></div>
</opendev:isIE>