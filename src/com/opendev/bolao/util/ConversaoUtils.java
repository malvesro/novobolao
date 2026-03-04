package com.opendev.bolao.util;

import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public final class ConversaoUtils {
	
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
	private static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");

    static {
        TimeZone timeZone = BolaoTime.getTimeZone();
        DATE_FORMAT.setTimeZone(timeZone);
        TIME_FORMAT.setTimeZone(timeZone);
    }

	
	public static Date converterParaData(String s) {
		Date dt = null;
		try { dt = DATE_FORMAT.parse(s); } catch (ParseException e) {}
		return dt;
	}
	
    public static Time converterParaTempo(String s) {
        Time time = null;
        try { time = new Time(TIME_FORMAT.parse(s).getTime()); } catch (ParseException e) {}
        return time;
    }
    
    public static Time converterParaTempo(int hora) {
        Time time = null;
        try { time = new Time(TIME_FORMAT.parse(Integer.toString(hora) + ":00").getTime()); } catch (ParseException e) {}
        return time;
    }
	
	public static int converterHoraParaInteiro(Time t) {
		String hora = t.toString();
		hora = hora.substring(0, hora.indexOf(":"));
		return Integer.parseInt(hora);
	}
	
	public static String converterParaString(Date data) {
		return DATE_FORMAT.format(data);
	}
	
	public static String converterParaString(Time hora) {
		return TIME_FORMAT.format(hora);
	}
}
