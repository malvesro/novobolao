<c:choose>
	<c:when test="${not empty pageContext.request.userPrincipal}">
		<c:redirect url="/seguro/principal.action" />
	</c:when>
	<c:otherwise>
		<c:redirect url="/login.action" />
	</c:otherwise>
</c:choose>
