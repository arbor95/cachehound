package de.cachehound.imp.mail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import CacheWolf.CacheHolder;
import CacheWolf.InfoBox;
import CacheWolf.Log;
import CacheWolf.Preferences;
import CacheWolf.Profile;
import CacheWolf.imp.GPXImporter;
import CacheWolf.imp.SpiderGC;
import de.cachehound.types.LogType;
import de.cachehound.util.SpiderService;

public class CacheWolfMailHandler implements IGCMailHandler {

	private Preferences prefs;
	private Profile profile;

	private static Logger logger = LoggerFactory
			.getLogger(CacheWolfMailHandler.class);

	private boolean spiderIfNotExists = true; // should be in the preferences,
	// perhaps for every logtype?

	public CacheWolfMailHandler(Preferences pf, Profile prof) {
		this.prefs = pf;
		this.profile = prof;
	}

	public boolean archived(String gcNumber, Message message, String subject,
			String text) {
		CacheHolder holder = getCacheHolder(gcNumber);
		if (holder == null) {
			return false;
		}
		holder.setArchived(true);
		holder.save();
		return addLogEntry(gcNumber, LogType.ARCHIVE, text, spiderIfNotExists);
	}

	public boolean disabled(String gcNumber, Message message, String subject,
			String text) {
		CacheHolder holder = getCacheHolder(gcNumber);
		if (holder == null) {
			return false;
		}
		holder.setAvailable(false);
		holder.save();
		return addLogEntry(gcNumber, LogType.DISABLE_LISTING, text,
				spiderIfNotExists);
	}

	public boolean enabled(String gcNumber, Message message, String subject,
			String text) {
		CacheHolder holder = getCacheHolder(gcNumber);
		if (holder == null) {
			return false;
		}
		holder.setAvailable(true);
		holder.save();
		return addLogEntry(gcNumber, LogType.ENABLE_LISTING, text,
				spiderIfNotExists);
	}

	public boolean unarchived(String gcNumber, Message message, String subject,
			String text) {
		CacheHolder holder = getCacheHolder(gcNumber);
		if (holder == null) {
			return false;
		}
		holder.setArchived(false);
		holder.save();
		return addLogEntry(gcNumber, LogType.UNARCHIVE, text, spiderIfNotExists);
	}

	public boolean retracted(String gcNumber, Message message, String subject,
			String text) {
		CacheHolder holder = getCacheHolder(gcNumber);
		if (holder == null) {
			return false;
		}
		holder.setArchived(true);
		holder.save();
		return addLogEntry(gcNumber, LogType.RETRACT, text, spiderIfNotExists);
	}

	public boolean published(String gcNumber, Message message, String subject,
			String text) {
		CacheHolder holder = getCacheHolder(gcNumber);
		if (holder == null) {
			// always spider a published cache
			holder = new CacheHolder(gcNumber);
			holder.getCacheDetails(true); // work around
			profile.cacheDB.add(holder);
		}
		return updateCache(holder);
	}

	public boolean updated(String gcNumber, Message message, String subject,
			String text) {
		CacheHolder holder = getCacheHolder(gcNumber);
		if (holder == null) {
			return false;
		}
		return updateCache(holder);
	}

	@Override
	public boolean didNotFound(String gcNumber, Message message,
			String subject, String text) {
		return addLogEntry(gcNumber, LogType.DID_NOT_FOUND, text,
				spiderIfNotExists);
	}

	@Override
	public boolean found(String gcNumber, Message message, String subject,
			String text) {
		return addLogEntry(gcNumber, LogType.FOUND, text, spiderIfNotExists);
	}

	@Override
	public boolean needMaintenance(String gcNumber, Message message,
			String subject, String text) {
		CacheHolder holder = getCacheHolder(gcNumber);
		if (holder == null) {
			return false;
		}
		holder.getFreshDetails().attributes.add("firstaid-yes.gif");
		holder.setUpdated(true);
		holder.save();
		return addLogEntry(gcNumber, LogType.NEEDS_MAINTENANCE, text,
				spiderIfNotExists);
	}

	@Override
	public boolean maintenancePferformed(String gcNumber, Message message,
			String subject, String text) {
		CacheHolder holder = getCacheHolder(gcNumber);
		if (holder == null) {
			return false;
		}
		updateCache(holder); // for removing needs maintenance attribute and
		// perhaps there is something new in the
		// description
		return addLogEntry(gcNumber, LogType.MAINTENANCE_DONE, text,
				spiderIfNotExists);
	}

	public boolean handlePocketQuery(Message message, String subject)
			throws MessagingException, IOException {
		Multipart mp = (Multipart) message.getContent();

		for (int j = 0; j < mp.getCount(); j++) {
			Part part = mp.getBodyPart(j);
			String disposition = part.getDisposition();

			if (disposition == null) {
				MimeBodyPart mimePart = (MimeBodyPart) part;
				if (mimePart.isMimeType("APPLICATION/ZIP")) {
					File file = new File("mail-tmp.zip");
					InputStream in = mimePart.getInputStream();
					FileOutputStream outputStream = new FileOutputStream(file);
					byte[] block = new byte[1024];
					int len;
					while (-1 != (len = in.read(block))) {
						outputStream.write(block, 0, len);
					}
					outputStream.close();
					in.close();

					GPXImporter gpx = new GPXImporter(prefs, profile, file
							.getName());
					gpx.doIt(0);

					file.delete();
				}
			}
		}

		return true;
	}

	private Log createLogEntry(LogType logType, String messageText) {
		// parse Date
		int indexOfDate = messageText.indexOf("Log Date: ");
		int indexOfEndDate = messageText.indexOf('\n', indexOfDate);
		String dateString = messageText.substring(indexOfDate + 10,
				indexOfEndDate);
		StringTokenizer tokenizer = new StringTokenizer(dateString, "/");
		String month = tokenizer.nextToken();
		month = month.length() > 1 ? month : "0" + month;
		String day = tokenizer.nextToken();
		day = day.length() > 1 ? day : "0" + day;
		String year = tokenizer.nextToken();
		String date = year + "-" + month + "-" + day;

		int indexOfFooter = messageText.indexOf("Visit this log entry",
				indexOfEndDate);
		String logText = messageText.substring(indexOfEndDate + 1,
				indexOfFooter - 2);

		logText = logText.replace("\n", "<br />");

		int indexOfProfile = messageText.indexOf("Profile for ");
		int indexOfEndProfile = messageText.indexOf('\n', indexOfProfile);
		String logger = messageText.substring(indexOfProfile + 12,
				indexOfEndProfile - 1);

		return new Log(logType, date, logger, logText);
	}

	private boolean addLogEntry(String gcNumber, LogType logType,
			String messageText, boolean spiderCacheIfNotExisting) {
		CacheHolder holder = getCacheHolder(gcNumber);
		if (holder == null) {
			return false;
		}
		Log log = createLogEntry(logType, messageText);
		holder.getFreshDetails().CacheLogs.add(log);
		holder.setLog_updated(true);
		holder.save();
		return true;
	}

	private boolean updateCache(CacheHolder holder) {
		SpiderService spider = SpiderService.getInstance();

		boolean loadAllLogs = false;

		InfoBox infB = new InfoBox("Info", "Loading",
				InfoBox.PROGRESS_WITH_WARNINGS);
		int test = spider.spiderSingle(holder, infB, loadAllLogs);
		if (test == SpiderGC.SPIDER_CANCEL) {
			infB.close(0);
			logger.info("Spidering canceled: {}", holder.getCacheID());
			return false;
		} else if (test == SpiderGC.SPIDER_ERROR) {
			logger.error("Spidering failed: {}", holder.getCacheID());
			return false;
		} else if (test == SpiderGC.SPIDER_IGNORE_PREMIUM) {
			logger.info("Spidering failed due to PM-only-cache: {}", holder
					.getCacheID());
			return false;
		} else {
			return true;
		}
	}

	private CacheHolder getCacheHolder(String gcNumber) {
		CacheHolder holder = profile.cacheDB.get(gcNumber);
		if (holder == null && spiderIfNotExists) {
			holder = new CacheHolder(gcNumber);
			holder.getCacheDetails(true); // work around
			profile.cacheDB.add(holder);
			updateCache(holder);
		}
		return holder;
	}

}
