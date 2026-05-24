package com.opendev.bolao.util;

import jakarta.persistence.Query;

public interface FiltroBusca {
	
	public String getHqlQuery();
	
	public Query popularParametrosDaHql(Query query);

}
