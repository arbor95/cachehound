package CacheWolf;

import de.cachehound.types.LogType;

public class Log {
	private static String MAXLOGICON = "MAXLOG";
	private static String INVALIDLOGICON = null;
	/** The LogType of this Log */
	private LogType logType;
	/** The date in format yyyy-mm-dd */
	private String date;
	/** The person who logged the cache */
	private String logger;
	/** The logged message */
	private String message;
	/** true, if the logger recommended the cache */
	private boolean recommended = false;

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
	public Log(String logLine) {
		// RECOMMENDED="1"<img src='icon_smile.gif'>&nbsp;2007-01-14 xyz<br>a
		// wonderful log
		try {
			int ic1 = logLine.indexOf("RECOMMENDED=\"1\"");
			if (ic1 >= 0)
				recommended = true;
			else
				recommended = false;
			ic1 = logLine.indexOf("<img src='");
			int ic2 = logLine.indexOf("'", ic1 + 10);
			String iconString = logLine.substring(ic1 + 10, ic2);
			int indexLogType = logLine.indexOf("logType='");
			if (indexLogType != -1) {
				int indexEndLogType = logLine.indexOf("'", indexLogType + 9);
				logType = logType.valueOf(logLine.substring(indexLogType + 9,
						indexEndLogType));
			} else {
				logType = LogType.getLogTypeFromIconString(iconString);
			}
			int d1 = logLine.indexOf(";");
			date = logLine.substring(d1 + 1, d1 + 11);
			int l1 = d1 + 12;
			if (logLine.substring(l1, l1 + 3).equals("by "))
				l1 += 3;
			int l2 = logLine.indexOf("<br>", l1);
			logger = logLine.substring(l1, l2);
			message = logLine.substring(l2 + 4, logLine.indexOf("]]>", l1));
		} catch (Exception ex) {
			if (logLine.indexOf("<img") < 0) { // Have we reached the line that
				// states max logs reached
				// iconString = MAXLOGICON;
				logType = LogType.UNKNOWN;
			} else {
				Global.getPref().log("Error parsing log: " + logLine);
				logType = LogType.UNKNOWN;
				// iconString = INVALIDLOGICON;
			}
			date = "1900-00-00";
			logger = message = "";
		}
	}

	public Log(LogType logType, String date, String logger, String message) {
		this(logType, date, logger, message, false);
	}

	public Log(LogType logType, String date, String logger, String message,
			boolean recommended_) {
		// this.iconString = icon;
		this.logType = logType;
		this.date = date;
		this.logger = logger;
		this.message = message.trim();
		this.recommended = recommended_;
	}

	public static Log maxLog() {
		return new Log(LogType.UNKNOWN, "1900-00-00", "CacheHound",
				"Die maximale Anzahl an Logs wurde erreicht.");
	}

	// public String getIcon() {
	// return logType.toIconString();
	// }

	public void setLogType(LogType logType) {
		this.logType = logType;
	}

	public LogType getLogType() {
		return logType;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getLogger() {
		return logger;
	}

	public void setLogger(String logger) {
		this.logger = logger;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message.trim();
	}

	public boolean isRecomended() {
		return recommended;
	}

	public boolean isFoundLog() {
		return logType == LogType.FOUND;
	}

	/** log was written by one of the aliases defined in preferences */
	public boolean isOwnLog() {
		return this.logger.equalsIgnoreCase(Global.getPref().myAlias)
				|| this.logger.equalsIgnoreCase(Global.getPref().myAlias2);
	}

	/** Return XML representation of log for storing in cache.xml */
	public String toXML() {
		StringBuffer s = new StringBuffer(400);
		s.append("<LOG>");
		if (recommended)
			s.append("RECOMMENDED=\"1\"");
		s.append("<![CDATA[");
		s.append(toHtml());
		s.append("]]>)");
		s.append("</LOG>\r\n");
		return s.toString();
	}

	/** Return HTML representation of log for display on screen */
	public String toHtml() {
		// Example: <img src='icon_smile.gif'>&nbsp;2007-01-14 xyz<br>a
		// wonderful log

		// if (iconString.equals(MAXLOGICON))
		// return "<hr>" + MyLocale.getMsg(736, "Too many logs") + "<hr>";

		StringBuffer s = new StringBuffer(300);
		s.append("<img src='" + logType.toIconString() + "' logType='"
				+ logType.toString() + "'>");
		if (recommended)
			s.append("<img src='recommendedlog.gif'>");
		s.append("&nbsp;");
		s.append(date);
		s.append(" by ");
		s.append(logger);
		s.append("<br>");
		s.append(message.trim());
		return s.toString();
	}
}
