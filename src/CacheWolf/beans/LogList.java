package CacheWolf.beans;

import java.util.ArrayList;
import java.util.List;


import de.cachehound.types.LogType;


public class LogList {
	/**
	 * The Vector containing the Log objects The list is always sorted in
	 * descending order
	 */
	private List<Log> logList = new ArrayList<Log>(10);
	private static final StringBuffer buffer = new StringBuffer();

	/** only valid after calling calcRecommendations() */
	private int numRecommended = -1;
	/** only valid after calling calcRecommendations() */
	private int foundsSinceRecommendation = -1;
	/** only valid after calling calcRecommendations() */
	private int recommendationRating = -1;

	
	/** Construct an empty Log list */
	public LogList() { // Public constructor
	}

	/** Get the Log at a certain position in the list */
	public Log getLog(int i) {
		return logList.get(i);
	}

	/** Return the size of the list */
	public int size() {
		return logList.size();
	}

	/** Clear the Log list */
	public void clear() {
		logList.clear();
	}

	/** 
	 * Add a Log to the list 
	 * @return the position where the log was placed or -1 if it is already in
	 *         the list
	 */
	public int add(Log log) {
		if (log.getLogType() != null) {
			return merge(log);
			//logList.add(log); // Don't add invalid logs
		}
		return -1;
	}

	/** Remove a Log from the list */
	public void remove(int i) {
		logList.remove(i);
	}

	/** Replace a Log in the list */
	public void replace(int i, Log log) {
		logList.set(i, log);
	}

	/**
	 * Merge a log into the list at the appropriate position
	 * 
	 * @param newLog
	 * @return the position where the log was placed or -1 if it is already in
	 *         the list
	 */

	private int merge(Log newLog) {
		String newDate = newLog.getDate();
		int size = size();
		int i;
		for (i = 0; i < size; i++) {
			int comp = newDate
					.compareTo(logList.get(i).getDate());
			if (comp > 0) {
				logList.add(i, newLog);
				return i;
			}
			if (comp == 0)
				break;
		}
		// Now i points to the first log with same date as the new log or
		// i==size()
		if (i == size) {
			logList.add(newLog);
			return size;
		}
		int firstLog = i;
		// Check whether we already have this log.
		while (i < size
				&& newDate.equals(logList.get(i).getDate())) {
			Log oldLog = logList.get(i);
			if (newLog.equals(oldLog)) {
				return -1; // Log already in list
			}
			i++;
		}
		if (i == size) {
			logList.add(newLog);
			return i;
		} else {
			logList.add(firstLog, newLog);
			return firstLog;
		}
	}

	/**
	 * Count the number of not-found logs
	 */
	public byte countNotFoundLogs() {
		byte countNoFoundLogs = 0;
		while (countNoFoundLogs < size() && countNoFoundLogs < 5) {
			if (getLog(countNoFoundLogs).getLogType() == LogType.DID_NOT_FOUND) {
				countNoFoundLogs++;
			} else
				break;
		}
		return countNoFoundLogs;
	}


	
	public int getNumRecommended() {
		if (numRecommended == -1) {
			calcRecommendations();
		}
		return numRecommended;
	}

	public int getFoundsSinceRecommendation() {
		if (numRecommended == -1) {
			calcRecommendations();
		}
		return foundsSinceRecommendation;
	}

	public int getRecommendationRating() {
		if (numRecommended == -1) {
			calcRecommendations();
		}
		return recommendationRating;
	}

	/**
	 * call this to
	 * 
	 */
	public void calcRecommendations() {
		numRecommended = 0;
		foundsSinceRecommendation = 0;
		Log l;
		int s = size();
		int i;
		for (i = 0; i < s; i++) {
			l = getLog(i);
			if (l.getDate().compareTo("2007-01-14") < 0)
				break; // this is the date when the recommendation system was
			// introdueced in opencaching.de see:
			// http://www.geoclub.de/viewtopic.php?t=14901&highlight=formel
			if (l.isRecomended())
				numRecommended++;
			if (l.isFoundLog())
				foundsSinceRecommendation++;
		}
		recommendationRating = getScore(numRecommended,
				foundsSinceRecommendation);
	}

	public static int getScore(int numrecommends, int numfoundlogs) {
		return Math
				.round((((float) numrecommends * (float) numrecommends + 1f) / (numfoundlogs / 10f + 1f)) * 100f);
	}

	/**
	 * Returns a simple concatenation of all Log texts of the list. Intended for
	 * text search in Logs.
	 * 
	 * @return All log messages
	 */
	public String allMessages() {
		buffer.setLength(0);
		for (int i = 0; i < logList.size(); i++) {
			buffer.append(logList.get(i).getMessage());
		}
		return buffer.toString();
	}

	/**
	 * trim down number of log to maximum number user wants to keep in database
	 * 
	 * @return number of removed logs
	 */
	public int purgeLogs() {
		int maxKeep = Global.getPref().maxLogsToKeep;
		boolean keepOwn = Global.getPref().alwaysKeepOwnLogs;
		int purgedLogs = 0;
		for (int i = logList.size(); i > maxKeep; i--) {
			if (!(keepOwn && getLog(i - 1).isOwnLog())) {
				this.remove(i - 1);
				purgedLogs++;
			}
		}
		return purgedLogs;
	}

}
