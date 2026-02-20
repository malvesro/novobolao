<div style="height: 90px;"></div>
<c:if test="${not empty param.status and param.status eq 'invalido'}">
	<style>
		#login_error {
			opacity: 1;
			transition: opacity 0.2s ease-in-out;
		}
		#login_error.fade-out {
			opacity: 0;
			transition: opacity 1s ease-in-out;
		}
	</style>
	<span id="login_error" class="error">
		<span><img alt="Erro" src="${base}/img/error.gif" style="vertical-align: top;" /></span>
		<span>
			<fmt:message key="login.error" />
		</span>
	</span>
	<script type="text/javascript">
		document.addEventListener('DOMContentLoaded', function () {
			var error = document.getElementById('login_error');
			if (!error) {
				return;
			}
			error.style.opacity = '1';
			var pulses = [200, 400, 600, 800];
			pulses.forEach(function (delay, index) {
				window.setTimeout(function () {
					error.style.opacity = index % 2 === 0 ? '0' : '1';
				}, delay);
			});
			var timeoutHandle = window.setTimeout(function () {
				error.classList.add('fade-out');
			}, 6000);
			window.addEventListener('beforeunload', function () {
				window.clearTimeout(timeoutHandle);
			});
		});
	</script>
	<br />
</c:if>
<opendev:portlet id="loginportlet" icon="/img/lock.png" title="Login" style="margin: 0 auto;">
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
