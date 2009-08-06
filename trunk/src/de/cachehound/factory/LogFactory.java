package de.cachehound.factory;

import CacheWolf.Global;
import de.cachehound.beans.Log;
import de.cachehound.types.LogType;

/**
 * Some creation and helping Methods for the bean-class Log.
 * 
 * Because of the helping Methods I'm think there have to be a better name or
 * there should be a second class with them in it. On the other hand I don't
 * want to slit code for creating and parsing Strings.
 * 
 * @author tweety
 */
public class LogFactory {

	private static LogFactory logFactory = new LogFactory();

	private LogFactory() {
		// Singleton Pattern
	}

	public static LogFactory getInstance() {
		return logFactory;
	}

	/** log was written by one of the aliases defined in preferences */
	// TODO: False place for this and an idiot implementation ...
	public boolean isOwnLog(Log log) {
		return Global.getPref().isMyAlias(log.getLogger());
	}

	/**
	 * Create a log from a single line in format<br>
	 * 
	 * <pre>
	 * RECOMMENDED=&quot;1&quot;&lt;img src='ICON'&gt; DATE LOGGER&lt;br&gt;MESSAGE
	 * or &lt;img src='ICON'&gt; DATE by LOGGER&lt;br&gt;MESSAGE
	 * </pre>
	 * 
	 * @param logLine
	 */
	public Log createFromProfileLine(String profileLine) {
		// RECOMMENDED="1"<img src='icon_smile.gif'>&nbsp;2007-01-14 xyz<br>a
		// wonderful log
		Log log = new Log();
		try {
			int ic1 = profileLine.indexOf("RECOMMENDED=\"1\"");
			log.setRecommended(ic1 >= 0);

			// This part is for being compatible with CacheWolf Profiles
			ic1 = profileLine.indexOf("<img src='");
			int ic2 = profileLine.indexOf("'", ic1 + 10);
			String iconString = profileLine.substring(ic1 + 10, ic2);
			int indexLogType = profileLine.indexOf("logType='");
			if (indexLogType != -1) {
				int indexEndLogType = profileLine
						.indexOf("'", indexLogType + 9);
				log.setLogType(LogType.valueOf(profileLine.substring(
						indexLogType + 9, indexEndLogType)));
			} else {
				log.setLogType(LogType.getLogTypeFromIconString(iconString));
			}
			int d1 = profileLine.indexOf(";");
			log.setDate(profileLine.substring(d1 + 1, d1 + 11));
			int l1 = d1 + 12;
			if (profileLine.substring(l1, l1 + 3).equals("by "))
				l1 += 3;
			int l2 = profileLine.indexOf("<br>", l1);
			log.setLogger(profileLine.substring(l1, l2));
			log.setMessage(profileLine.substring(l2 + 4, profileLine.indexOf(
					"]]>", l1)));
			return log;
		} catch (Exception ex) {
			if (profileLine.indexOf("<img") < 0) { // Have we reached the line
				// that
				// states max logs reached
				return null;
				// return createMaxLog();
			} else {
				Global.getPref().log("Error parsing log: " + profileLine);
				return null;
			}

		}
	}

	public Log createMaxLog() {
		Log log = new Log();
		log.setLogType(LogType.UNKNOWN);
		log.setDate("1900-00-00");
		log.setLogger("CacheHound");
		log.setMessage("Die maximale Anzahl an Logs wurde erreicht.");
		return log;
	}

	public Log createLog(LogType logType, String date, String logger,
			String message) {
		Log log = new Log();
		log.setLogType(logType);
		log.setDate(date);
		log.setLogger(logger);
		log.setMessage(message);
		return log;
	}

	public Log createLog(LogType logType, String date, String logger,
			String message, boolean recommended) {
		Log log = new Log();
		log.setLogType(logType);
		log.setDate(date);
		log.setLogger(logger);
		log.setMessage(message);
		log.setRecommended(recommended);
		return log;
	}

	/** Return XML representation of log for storing in cache.xml */

	public String toXMLSnippet(Log log) {
		StringBuilder sb = new StringBuilder();
		sb.append("<LOG>");
		if (log.isRecommended()) {
			sb.append("RECOMMENDED=\"1\"");
		}
		sb.append("<![CDATA[");
		sb.append(toHtml(log));
		sb.append("]]>)");
		sb.append("</LOG>\r\n");
		return sb.toString();

	}

	/** Return HTML representation of log for display on screen */
	public String toHtml(Log log) {
		// Example: <img src='icon_smile.gif'>&nbsp;2007-01-14 xyz<br>a
		// wonderful log

		// if (iconString.equals(MAXLOGICON))
		// return "<hr>" + MyLocale.getMsg(736, "Too many logs") + "<hr>";

		StringBuilder sb = new StringBuilder();
		sb.append("<img src='" + log.getLogType().toIconString()
				+ "' logType='" + log.getLogType().toString() + "'>");
		if (log.isRecommended())
			sb.append("<img src='recommendedlog.gif' />");
		sb.append("&nbsp;");
		sb.append(log.getDate());
		sb.append(" by ");
		sb.append(log.getLogger());
		sb.append("<br>");
		sb.append(log.getMessage().trim());
		return sb.toString();
	}

}
