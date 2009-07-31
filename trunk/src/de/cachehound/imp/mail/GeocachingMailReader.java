package de.cachehound.imp.mail;

import java.io.IOException;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Flags.Flag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import CacheWolf.Preferences;

public class GeocachingMailReader {

	private String protocol;
	private String host;
	private String user;
	private String password;
	private String inBox;
	private String outBox = "CacheHoundReaded";
	private boolean readOnly;
	private boolean moveMessages;
	private boolean markMessagesReaded;
	private boolean deleteMessages;
	
	private Store store;
	private Folder inFolder;
	private Folder outFolder;
	private IGCMailHandler handler;
	
	private static Logger logger = LoggerFactory
			.getLogger(GeocachingMailReader.class);

	public GeocachingMailReader(Preferences prefs, IGCMailHandler handler) {
		this.protocol = prefs.mailProtocol;
		this.host = prefs.mailHost;
		this.user = prefs.mailLoginName;
		this.password = prefs.mailPassword;
		this.inBox = prefs.mailInbox;
		this.outBox = prefs.mailMoveBox;
		this.moveMessages = prefs.mailMoveMessages;
		this.markMessagesReaded = prefs.mailMarkMailsAsReaded;
		this.deleteMessages = prefs.mailDeleteMessages;
		this.handler = handler;
	}

	public void connect(boolean readOnly) throws MessagingException {
		Session session = Session.getInstance(new Properties());
		session.setDebug(false);
		store = session.getStore(protocol);
		store.connect(host, user, password);
		inFolder = store.getFolder(inBox);
		if (protocol.toLowerCase().startsWith("imap")) {
			outFolder = store.getFolder(outBox);
			if (! outFolder.exists()) {
				boolean created = outFolder.create(Folder.HOLDS_MESSAGES);
				if (!created) {				
					logger.error("Could not create Folder {}.", outBox);
					outFolder = null;
				}
			}
			outFolder.open(Folder.READ_WRITE);
		}
		// open Pop Mailboxes in ReadOnly-Mode
		if (readOnly || protocol.toLowerCase().startsWith("pop")) {
			inFolder.open(Folder.READ_ONLY);
		} else {
			inFolder.open(Folder.READ_WRITE);
		}
	}

	public void readMessages(boolean onlyNew)
			throws MessagingException, IOException {
		int countMessages = inFolder.getMessageCount();
		logger.info("Found {} Caches in Mailbox {}:{}", new Object[] {countMessages, host, inBox});

		// TODO: Weiter entwickeln
//		HoldFolderOpenThread holdOpenThread = new HoldFolderOpenThread();
//		holdOpenThread.start();
		
		Message message = null;
		for (int i = 0; i < countMessages; i++) {
			
			message = inFolder.getMessage(i + 1);
			boolean newMessage = !message.getFlags().contains(Flag.SEEN);
			if (newMessage || !onlyNew) {
				readMessage(message);
			}
			// TODO: Rausnehmen, nur für TESTS!!!
//			message.getContent();
//			try {
//				Thread.sleep(930 * 1000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			System.out.println("Nachricht i+1 'abgewartet'");
		}
//		holdOpenThread.stopFolderOpenThread();
	}

	private void readMessage(Message message)
			throws MessagingException, IOException {
		String subject = message.getSubject();
		logger.debug("Reading Message: {}", subject);
		boolean readed = false;
		if (subject.startsWith("[GEO] Pocket Query:")) {
			logger.debug("Handle Pocket Query: {}", subject);
			readed = handler.handlePocketQuery(message, subject);
		} else if (subject.startsWith("[GEO] Notify:")) {
			logger.debug("Handle Notify: {}", subject);
			readed = readNotify(message, subject);
		} else {
			logger.warn("Can't parse Subject of Message: {}", subject);
			return;
		}
		if (readed) {
			if (markMessagesReaded) {
				message.setFlag(Flag.SEEN, true);
			}
			if (moveMessages) {
				if (outFolder != null) {
					inFolder.copyMessages(new Message[] {message}, outFolder);
					message.setFlag(Flag.DELETED, true);
				}
			}
			if (deleteMessages) {
				message.setFlag(Flag.DELETED, true);
			}
		}
	}

	private boolean readNotify(Message message, String subject)
			throws IOException, MessagingException {

		String text = message.getContent().toString();
		int intexFor = text.indexOf("For ");
		String gcNumber = text.substring(intexFor + 4, intexFor + 11);
		if (gcNumber.endsWith(":")) {
			gcNumber = gcNumber.substring(0, 6);
		}

		if (subject.contains(" archived ") && subject.contains(" (Archived) ")) {
			return handler.archived(gcNumber, message, subject, text);
		} else if (subject.contains(" published ")) {
			return handler.published(gcNumber, message, subject, text);
		} else if (subject.contains(" enabled ")) {
			return handler.enabled(gcNumber, message, subject, text);
		} else if (subject.contains(" unarchived ")) {
			return handler.unarchived(gcNumber, message, subject, text);
		} else if (subject.contains("retract")) { // noch nie gesehen ... aber
			// soll ja vorkommen können
			// ...
			return handler.retracted(gcNumber, message, subject, text);
		} else if (subject.contains(" disabled ")) {
			return handler.disabled(gcNumber, message, subject, text);
		} else if (subject.contains(" found ")) {
			return handler.found(gcNumber, message, subject, text);
		} else if (subject.contains(" couldn't find ")) {
			return handler.didNotFound(gcNumber, message, subject, text);
		} else if (subject.contains(" updated the coordinates ")) {
			return handler.updated(gcNumber, message, subject, text);
		} else if (subject.contains(" reported ")
				&& subject.contains(" needs maintenance")) {
			return handler.needMaintenance(gcNumber, message, subject, text);
		} else if (subject.contains(" performed maintenance for ")) {
			return handler.maintenancePferformed(gcNumber, message, subject, text);
		} else { // hmm, doch keine Mail mit der wir was anfangen können?
			logger.error("Notify-Message-Header not parsable: {}", subject);
			return false;
		}
		
	}

	public void disconnect(boolean write) throws MessagingException {
		inFolder.close(write);
		if (outFolder != null) {
			outFolder.close(write);
		}
		store.close();
	}

	private class HoldFolderOpenThread extends Thread {

		private boolean active = true;
				
		public void run() {
			while (active) {
				try {
					inFolder.getMessageCount();
				} catch (MessagingException e) {
					logger.error("HoldFolderOpen get's a failure.", e);
					return;
				}
				try {
					sleep(30 * 1000);
				} catch (InterruptedException e) {
					// nothing to do;
				}
			}
		}
		
		public void stopFolderOpenThread() {
			active = false;
			this.interrupt();
		}
		
	}
	
}
