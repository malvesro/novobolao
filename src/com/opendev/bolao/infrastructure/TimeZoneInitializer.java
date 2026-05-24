package com.opendev.bolao.infrastructure;

import java.util.TimeZone;

import com.opendev.bolao.util.BolaoTime;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

/**
 * Define a zona horária padrão para todo o runtime.
 */
public class TimeZoneInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        TimeZone.setDefault(BolaoTime.getTimeZone());
    }
}
