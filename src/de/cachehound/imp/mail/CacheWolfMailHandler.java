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

import CacheWolf.CacheHolder;
import CacheWolf.Global;
import CacheWolf.InfoBox;
import CacheWolf.Log;
import CacheWolf.Preferences;
import CacheWolf.Profile;
import CacheWolf.imp.GPXImporter;
import CacheWolf.imp.SpiderGC;

public class CacheWolfMailHandler extends DummyGCMailHandler {

	private Preferences prefs;
	private Profile profile;

	public CacheWolfMailHandler(Preferences pf, Profile prof) {
		this.prefs = pf;
		this.profile = prof;
	}

	private Log createLog(String icon, String messageText) {
		// parse Date
		int indexOfDate = messageText.indexOf("Log Date: ");
		int indexOfEndDate = messageText.indexOf('\n', indexOfDate);
		String dateString = messageText.substring(indexOfDate + 10 , indexOfEndDate);
		StringTokenizer tokenizer = new StringTokenizer(dateString, "/");
		String day = tokenizer.nextToken();
		day = day.length() > 1 ? day : "0" + day;
		String month = tokenizer.nextToken();
		month = month.length() > 1 ? month : "0" + month;
		String year = tokenizer.nextToken();
		String date = year + "-" + month + "-" + day;
		
		int indexOfFooter = messageText.indexOf("Visit this log entry", indexOfEndDate);
		String logText = messageText.substring(indexOfEndDate + 1, indexOfFooter - 2);
		
		
		
		// TODO: remove
		return null;
	}

		

	public boolean archived(String gcNumber, Message message, String subject,
			String text) {
		CacheHolder holder = profile.cacheDB.get(gcNumber);
		if (holder == null) {
			System.out.println("Konnte Cache nicht finden: " + gcNumber);
			return false;
		}
		holder.setArchived(true);
		System.out.println("Konnte Cache archivieren: " + gcNumber);
		
		return true;
		
	}

	public boolean disabled(String gcNumber, Message message, String subject,
			String text) {
		CacheHolder holder = profile.cacheDB.get(gcNumber);
		if (holder == null) {
			System.out.println("Konnte Cache nicht finden: " + gcNumber);
			return false;
		}
		holder.setAvailable(false);
		System.out.println("Konnte Cache disablen: " + gcNumber);
		return true;
	}

	public boolean enabled(String gcNumber, Message message, String subject,
			String text) {
		CacheHolder holder = profile.cacheDB.get(gcNumber);
		if (holder == null) {
			System.out.println("Konnte Cache nicht finden: " + gcNumber);
			return false;
		}
		holder.setAvailable(true);
		System.out.println("Konnte Cache enablen: " + gcNumber);
		return true;
	}

	public boolean unarchived(String gcNumber, Message message, String subject,
			String text) {
		CacheHolder holder = profile.cacheDB.get(gcNumber);
		if (holder == null) {
			System.out.println("Konnte Cache nicht finden: " + gcNumber);
			return false;
		}
		holder.setArchived(false);
		System.out.println("Konnte Cache unarchivieren: " + gcNumber);
		return true;
	}

	public boolean retracted(String gcNumber, Message message, String subject,
			String text) {
		CacheHolder holder = profile.cacheDB.get(gcNumber);
		if (holder == null) {
			System.out.println("Konnte Cache nicht finden: " + gcNumber);
			return false;
		}
		holder.setArchived(true);
		System.out
				.println("Konnte Cache zwar nicht retracten, aber archivieren: "
						+ gcNumber);
		return true;
	}

	public boolean published(String gcNumber, Message message, String subject,
			String text) {
		System.out.println("Published" + gcNumber);
		CacheHolder holder = profile.cacheDB.get(gcNumber);
		if (holder == null) {
			holder = new CacheHolder(gcNumber);
			holder.getCacheDetails(true); // work around
			profile.cacheDB.add(holder);
		}
		return updateCache(holder);
	}

	public boolean updated(String gcNumber, Message message, String subject,
			String text) {
		System.out.println("UPDATED: " + gcNumber);
		CacheHolder holder = profile.cacheDB.get(gcNumber);
		if (holder == null) {
			System.out.println("Konnte Cache nicht finden: " + gcNumber);
			return false;
		}
		return updateCache(holder);
	}

	private boolean updateCache(CacheHolder holder) {
		SpiderGC spider = new SpiderGC(prefs, profile, false);

		int index = profile.cacheDB.getIndex(holder);

		boolean forceLogin = Global.getPref().forceLogin; // To ensure that
															// spiderSingle only
															// logs in once if
															// forcedLogin=true
		boolean loadAllLogs = false;

		InfoBox infB = new InfoBox("Info", "Loading",
				InfoBox.PROGRESS_WITH_WARNINGS);
		int test = spider.spiderSingle(index, infB, forceLogin, loadAllLogs);
		if (test == SpiderGC.SPIDER_CANCEL) {
			infB.close(0);
			System.out.println("SPIDER_CANCLE");
			return false;
		} else if (test == SpiderGC.SPIDER_ERROR) {
			System.out.println("SPIDER_ERROR");
			return false;
		} else {
			System.out.println("Neue Daten im Cachewolf");
			return true;
		}
	}

	public boolean handlePocketQuery(Message message, String subject)
			throws MessagingException, IOException {
		Multipart mp = (Multipart) message.getContent();

		for (int j = 0; j < mp.getCount(); j++) {
			Part part = mp.getBodyPart(j);
			String disposition = part.getDisposition();

			if (disposition == null) {
				MimeBodyPart mimePart = (MimeBodyPart) part;
				System.out.println("PQuery " + j + ": "
						+ mimePart.getContentType());

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

		System.out.println("PQ konnte gelesen sein: " + subject);
		return true;
	}

}
