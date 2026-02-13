package com.opendev.bolao.util;

import org.hibernate.Query;

public interface FiltroBusca {
	
	public String getHqlQuery();
	
	public Query popularParametrosDaHql(Query query);

}
