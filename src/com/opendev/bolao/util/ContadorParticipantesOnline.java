package com.opendev.bolao.util;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;


public class ContadorParticipantesOnline implements HttpSessionListener {
    
    private static int online = 0;

    public void sessionCreated(HttpSessionEvent ev) {
        online++;
    }

    public void sessionDestroyed(HttpSessionEvent ev) {
        if (online > 0) {
            online--;
        }
    }
    
    public static int getNumeroDeParticipantesOnline() {
        return online;
    }

}
