package de.cachehound.imp.mail;

import java.io.IOException;

import javax.mail.Message;
import javax.mail.MessagingException;

public interface IGCMailHandler {

	public abstract boolean archived(String gcNumber, Message message,
			String subject, String text);

	public abstract boolean published(String gcNumber, Message message,
			String subject, String text);

	public abstract boolean enabled(String gcNumber, Message message,
			String subject, String text);

	public abstract boolean unarchived(String gcNumber, Message message,
			String subject, String text);

	public abstract boolean retracted(String gcNumber, Message message,
			String subject, String text);

	public abstract boolean disabled(String gcNumber, Message message,
			String subject, String text);

	public abstract boolean found(String gcNumber, Message message,
			String subject, String text);

	public abstract boolean didNotFound(String gcNumber, Message message,
			String subject, String text);

	public abstract boolean updated(String gcNumber, Message message,
			String subject, String text);

	public abstract boolean needMaintenance(String gcNumber, Message message,
			String subject, String text);

	public abstract boolean handlePocketQuery(Message message, String subject)
			throws MessagingException, IOException;

	public abstract boolean maintenancePerformed(String gcNumber,
			Message message, String subject, String text);

}