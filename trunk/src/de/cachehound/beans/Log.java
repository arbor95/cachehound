package de.cachehound.beans;

import de.cachehound.types.LogType;

public class Log {
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
		return isRecommended();
	}

	public boolean isFoundLog() {
		return logType == LogType.FOUND;
	}

	public void setRecommended(boolean recommended) {
		this.recommended = recommended;
	}

	public boolean isRecommended() {
		return recommended;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Log) {
			Log log = (Log) o;
			return logType == log.logType
					&& isRecommended() == log.isRecommended()
					&& date.equals(log.date) && logger.equals(log.logger)
					&& message.equals(log.message);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return ((date.hashCode() + logger.hashCode() * 17) + message.hashCode()
				* 17 + logType.hashCode());
	}
}
