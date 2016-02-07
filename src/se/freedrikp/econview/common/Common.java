package se.freedrikp.econview.common;

import java.util.Calendar;
import java.util.Date;

public class Common {

	public static Calendar getFlattenCalendar(Date date){
		Calendar cal = Calendar.getInstance();
		if (date != null){
			cal.setTime(date);
		}
		cal.set(Calendar.HOUR_OF_DAY,0);
		cal.set(Calendar.MINUTE,0);
		cal.set(Calendar.SECOND,0);
		cal.set(Calendar.MILLISECOND,0);
		return cal;
	}
	
}
