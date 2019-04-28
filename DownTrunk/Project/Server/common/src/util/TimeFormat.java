package util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeFormat {

	private static String time = "yyyy-MM-dd HH:mm:ss";

	private static String day = "yyyy-MM-dd";

	private static String millisTime = "yyyy-MM-dd HH:mm:ss,SSS";

	private static String ts = "yyyyMMddHHmmss";

	private static String tss = "yyyyMMddHHmmssSSS";

	public static String getTS() {
		return new SimpleDateFormat(ts).format(Calendar.getInstance().getTime());
	}
	
	public static String getTSS() {
		return new SimpleDateFormat(tss).format(Calendar.getInstance().getTime());
	}

	public static String getDate() {
		return new SimpleDateFormat(day).format(Calendar.getInstance().getTime());
	}

	public static String getTime() {
		return new SimpleDateFormat(time).format(Calendar.getInstance().getTime());
	}

	public static String getMillsTime() {
		return new SimpleDateFormat(millisTime).format(Calendar.getInstance().getTime());
	}

	public static Calendar getTimeByStr(String str) throws ParseException {
		Calendar calendar = Calendar.getInstance();
		Date date = new SimpleDateFormat(time).parse(str);
		calendar.setTime(date);
		return calendar;
	}

	public static String formatTimeByCalendar(Calendar calendar) {
		return new SimpleDateFormat(time).format(calendar.getTime());
	}

	public static String formatTimeByDate(Date date) {
		return new SimpleDateFormat(time).format(date);
	}

	public static Calendar getHourTime() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar;
	}

	public static Calendar setTime(Calendar calendar, int hour, int minute) {
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar;
	}

	public static String getUTCTime() {
		// 1、取得本地时间：
		Calendar cal = Calendar.getInstance();
		// 2、取得时间偏移量：
		int zoneOffset = cal.get(java.util.Calendar.ZONE_OFFSET);
		// 3、取得夏令时差：
		int dstOffset = cal.get(java.util.Calendar.DST_OFFSET);
		// 4、从本地时间里扣除这些差量，即可以取得UTC时间：
		cal.add(java.util.Calendar.MILLISECOND, -(zoneOffset + dstOffset));
		return new SimpleDateFormat(time).format(cal.getTime());
	}

	public static int getUTC() {
		// 1、取得本地时间：
		Calendar cal = Calendar.getInstance();
		// 2、取得时间偏移量：
		int zoneOffset = cal.get(java.util.Calendar.ZONE_OFFSET);
		return zoneOffset / 1000 / 3600;
	}
	
	public static int getTimeInSeconds() {
		Calendar cal = Calendar.getInstance();
		return (int) (cal.getTimeInMillis() / 1000);
	}

	public static String getCalendar(Calendar calendar, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(calendar.getTime());
	}

    /**
     * 解析为毫秒级时间
     * 
     * @param str
     * @return
     * @throws ParseException
     */
	public static long getTimeInMillis(String str) throws ParseException {
        Date date = new SimpleDateFormat(time).parse(str);
        return date.getTime();
    }
	
	/**
	 * 判断时间是否在两个时间之间
	 * 
	 * @param startTime
	 * @param endTime
	 * @param now
	 * @return
	 */
	public static boolean isBetweenTime(String startTime, String endTime, long now) {
		try {
			long start = TimeFormat.getTimeInMillis(startTime);
			long end = TimeFormat.getTimeInMillis(endTime);
			return now >= start && now < end;
		} catch (ParseException e) {
			ErrorPrint.print(e);
			return false;
		}
	}
	
	public static Calendar nextClearTime(Calendar time) {
		Calendar result = Calendar.getInstance();
		result.setTimeInMillis(time.getTimeInMillis());
		if (time.get(Calendar.HOUR_OF_DAY) < 3) {
			result.set(Calendar.MINUTE, 0);
			result.set(Calendar.SECOND, 0);
			return result;
		}
		
		result.add(Calendar.DAY_OF_YEAR, 1);
		result.set(Calendar.HOUR_OF_DAY, 3);
		result.set(Calendar.MINUTE, 0);
		result.set(Calendar.SECOND, 0);
		return result;
	}

	public static boolean isBetweenTime(long start, long end, long now) {
		return now >= start && now < end;
	}
}
