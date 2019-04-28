package util;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorPrint {

	private static final Logger logger = LoggerFactory.getLogger(Logger.class);

	public static void sysLogInitialized(String s) throws FileNotFoundException {
		String date = TimeFormat.getDate();
		// Error输出日志
		if (System.getProperty("logPath") != null) {
			StringBuilder path = new StringBuilder();
			path.append(System.getProperty("logPath")).append(s).append("ServerError_")
					.append(Calendar.getInstance().getTimeInMillis()).append("_").append(date).append(".log");
			PrintStream ps = new PrintStream(path.toString()) {
				public void println(Object x) {
					if (String.valueOf(x).lastIndexOf("(") == -1) {
						super.println(x + "  [" + TimeFormat.getMillsTime() + "]");
					} else {
						super.println(x);
					}
				}
			};
			System.setErr(ps);
		}

		// Out输出日志
		if (System.getProperty("logPath") != null) {
			StringBuilder path = new StringBuilder();
			path.append(System.getProperty("logPath")).append(s).append("ServerOut_")
					.append(Calendar.getInstance().getTimeInMillis()).append("_").append(date).append(".log");
			PrintStream ps = new PrintStream(path.toString()) {
				public void println(Object x) {
					super.println(x + "  [" + TimeFormat.getMillsTime() + "]");
				}
			};
			System.setOut(ps);
		}
	}

	public static void print(Throwable e) {
		e.printStackTrace();
		String text = getTrace(e);
		logger.error(text);
	}

	private static String getTrace(Throwable t) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter writer = new PrintWriter(stringWriter);
		t.printStackTrace(writer);
		StringBuffer buffer = stringWriter.getBuffer();
		return buffer.toString();
	}
}
