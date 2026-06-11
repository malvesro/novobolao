<div class="spacer-xl"></div>
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
	<span id="login_error" class="error" role="alert" aria-live="assertive">
		<span><img alt="Erro" src="${base}/img/error.gif" class="icon-inline-top" /></span>
		<span>
			<fmt:message key="login.error" />
		</span>
	</span>
	<script type="text/javascript" nonce="${cspNonce}">
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
<opendev:portlet id="loginportlet" icon="/img/lock.png" title="Login">
	<%-- Usa URL absoluta com contexto para manter o POST estável em qualquer namespace --%>
	<c:url var="loginProcessUrl" value="/login.action" />
	<form action="${loginProcessUrl}" method="post">
		<%-- Token CSRF explícito para ambientes com proteção estrita no Spring Security --%>
		<c:if test="${not empty _csrf}">
			<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
		</c:if>
			<div class="inner">
				<div class="form-grid">
					<div class="form-row">
						<label for="login_username">
							<fmt:message key="login.username" />
						</label>
						<input id="login_username" type="text" name="j_username" class="text" />
					</div>
					<div class="form-row">
						<label for="login_pwd">
							<fmt:message key="login.pwd" />
						</label>
						<input id="login_pwd" type="password" name="j_password" class="text" />
					</div>
					<div class="form-links">
						<c:url var="cadastroFormURL" value="/cadastroForm.action" />
						<c:url var="recuperarSenhaURL" value="/recuperarSenhaForm.action" />
						<a href="${cadastroFormURL}">
							<fmt:message key="login.signin" />
						</a>
						<span class="form-links-separator">|</span>
						<a href="${recuperarSenhaURL}" class="link-secondary">
							<fmt:message key="login.forgot" />
						</a>
					</div>
				</div>
			</div>
		<div class="footer">
			<fmt:message var="loginSubmitLabel" key="login.go" />
			<input type="submit" name="submit" class="button" value="${loginSubmitLabel}" />
		</div>
	</form>
</opendev:portlet>
