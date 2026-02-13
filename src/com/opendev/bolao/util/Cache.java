package com.opendev.bolao.util;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Cache implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Map cacheMap;
	private boolean expirado;
	
	public Cache() {
		this.cacheMap = Collections.synchronizedMap(new HashMap());
		setExpirado(true);
	}
	
	public Object get(Object id) {
		synchronized (this.cacheMap) {
			return this.cacheMap.get(id);
		}
	}
	
	public void put(Object id, Object cacheable) {
		synchronized (this.cacheMap) {
			this.cacheMap.put(id, cacheable);
		}		
	}

	public synchronized boolean isExpirado() {
		return expirado;
	}

	public synchronized void setExpirado(boolean expirado) {
		if (expirado) {
			this.cacheMap.clear();
		}
		this.expirado = expirado;
	}

}
