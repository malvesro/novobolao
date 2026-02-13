<c:choose>
	<c:when test="${not empty pageContext.request.userPrincipal}">
		<c:redirect url="/seguro/principal.jsp" />
	</c:when>
	<c:otherwise>
		<c:redirect url="/login.jsp" />
	</c:otherwise>
</c:choose>
