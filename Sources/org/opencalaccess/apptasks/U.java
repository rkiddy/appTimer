package org.opencalaccess.apptasks;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.webobjects.foundation.NSMutableDictionary;

public class U {

	public static final String APP_TASK_INTERVAL_DAILY = "DAILY";
	public static final String APP_TASK_INTERVAL_HOURLY = "HOURLY";
	public static final String APP_TASK_INTERVAL_MINUTELY = "MINUTELY";

	public static final String APP_TASK_INTERVAL_TWICE_DAILY = "TWICE_DAILY";

	public static long one_minute = 60 /* sec */ * 1000L;

	public static long one_hour = 60 /* min */ * 60 /* sec */ * 1000L;

	public static final NSMutableDictionary<String,Long> intervalTimes = new NSMutableDictionary<>();

	static {
		intervalTimes.put(APP_TASK_INTERVAL_MINUTELY, one_minute);
		intervalTimes.put(APP_TASK_INTERVAL_HOURLY, 60 * one_minute);
		intervalTimes.put(APP_TASK_INTERVAL_TWICE_DAILY, 12 * one_hour);
		intervalTimes.put(APP_TASK_INTERVAL_DAILY, 24 * one_hour);
	}

	public static final String APP_TASK_INTERVAL_PREFIX_MINUTES = "MINUTES_";
	public static final String APP_TASK_INTERVAL_PREFIX_HOURS = "HOURS_";

	public static final ThreadLocal<SimpleDateFormat> tdf = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
			fmt.setTimeZone(TimeZone.getTimeZone("PST"));
			return fmt;
		}
	};

	public static void log(final Object... words) {
		String className = (new Throwable()).getStackTrace()[1].getClassName();
		StringBuilder s = new StringBuilder();
		s.append(tdf.get().format(new Date()));
		s.append(" ");
		s.append(className);
		s.append(" ");
		for (Object str : words) {
			s.append(str);
		}
		System.out.println(s.toString());
	}
}
