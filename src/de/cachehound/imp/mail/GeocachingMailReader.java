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

public class GeocachingMailReader {

	private String protocol;
	private String host;
	private String user;
	private String password;
	private String mailbox;

	private Store store;
	private Folder folder;
	private IGCMailHandler handler;
	
	private static Logger logger = LoggerFactory
			.getLogger(GeocachingMailReader.class);

	public GeocachingMailReader(String protocol, String host, String user,
			String password, String mailbox, IGCMailHandler handler) {
		this.protocol = protocol;
		this.host = host;
		this.user = user;
		this.password = password;
		this.mailbox = mailbox;
		this.handler = handler;
	}

	public void connect(boolean readonly) throws MessagingException {
		Session session = Session.getInstance(new Properties());
		session.setDebug(false);
		store = session.getStore(protocol);
		store.connect(host, user, password);
		folder = store.getFolder(mailbox);
		// open Pop Mailboxes in ReadOnly-Mode
		if (readonly || protocol.toLowerCase().startsWith("pop")) {
			folder.open(Folder.READ_ONLY);
		} else {
			folder.open(Folder.READ_WRITE);
		}
	}

	public void readMessages(boolean onlyNew, boolean markAsRead, boolean remove)
			throws MessagingException, IOException {
		int countMessages = folder.getMessageCount();
		logger.info("Found {} Caches in Mailbox {}:{}", new Object[] {countMessages, host, mailbox});

		Message message = null;
		for (int i = 0; i < countMessages; i++) {
			message = folder.getMessage(i + 1);
			boolean newMessage = !message.getFlags().contains(Flag.SEEN);
			if (newMessage || !onlyNew) {
				readMessage(message, markAsRead, remove);
			}
		}
	}

	private void readMessage(Message message, boolean markAsRead, boolean remove)
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
			if (markAsRead) {
				message.setFlag(Flag.SEEN, true);
			}
			if (remove) {
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
		folder.close(write);
		store.close();
	}

}
