package de.cachehound.imp.mail;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;

public class DummyGCMailHandler implements IGCMailHandler {

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.flopl.geocaching.mail.IGCMailHandler#archived(java.lang.String,
	 * javax.mail.Message, java.lang.String, java.lang.String)
	 */
	public boolean archived(String gcNumber, Message message, String subject,
			String text) {
		System.out.println("archived " + gcNumber);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.flopl.geocaching.mail.IGCMailHandler#published(java.lang.String,
	 * javax.mail.Message, java.lang.String, java.lang.String)
	 */
	public boolean published(String gcNumber, Message message, String subject,
			String text) {
		System.out.println("published " + gcNumber);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.flopl.geocaching.mail.IGCMailHandler#enabled(java.lang.String,
	 * javax.mail.Message, java.lang.String, java.lang.String)
	 */
	public boolean enabled(String gcNumber, Message message, String subject,
			String text) {
		System.out.println("enabled " + gcNumber);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.flopl.geocaching.mail.IGCMailHandler#unarchived(java.lang.String,
	 * javax.mail.Message, java.lang.String, java.lang.String)
	 */
	public boolean unarchived(String gcNumber, Message message, String subject,
			String text) {
		System.out.println("unarchived " + gcNumber);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.flopl.geocaching.mail.IGCMailHandler#retracted(java.lang.String,
	 * javax.mail.Message, java.lang.String, java.lang.String)
	 */
	public boolean retracted(String gcNumber, Message message, String subject,
			String text) {
		System.out.println("retracted " + gcNumber);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.flopl.geocaching.mail.IGCMailHandler#disabled(java.lang.String,
	 * javax.mail.Message, java.lang.String, java.lang.String)
	 */
	public boolean disabled(String gcNumber, Message message, String subject,
			String text) {
		System.out.println("disabled " + gcNumber);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.flopl.geocaching.mail.IGCMailHandler#found(java.lang.String,
	 * javax.mail.Message, java.lang.String, java.lang.String)
	 */
	public boolean found(String gcNumber, Message message, String subject,
			String text) {
		System.out.println("found " + gcNumber);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.flopl.geocaching.mail.IGCMailHandler#didNotFound(java.lang.String,
	 * javax.mail.Message, java.lang.String, java.lang.String)
	 */
	public boolean didNotFound(String gcNumber, Message message,
			String subject, String text) {
		System.out.println("didNotFound " + gcNumber);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.flopl.geocaching.mail.IGCMailHandler#updated(java.lang.String,
	 * javax.mail.Message, java.lang.String, java.lang.String)
	 */
	public boolean updated(String gcNumber, Message message, String subject,
			String text) {
		System.out.println("updated " + gcNumber);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.flopl.geocaching.mail.IGCMailHandler#needMaintenance(java.lang.String,
	 * javax.mail.Message, java.lang.String, java.lang.String)
	 */
	public boolean needMaintenance(String gcNumber, Message message,
			String subject, String text) {
		System.out.println("needMaintenance " + gcNumber);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.flopl.geocaching.mail.IGCMailHandler#handlePocketQuery(javax.mail.
	 * Message, java.lang.String)
	 */
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

				// if (mimePart.isMimeType("text/plain")) {
				// BufferedReader in = new BufferedReader(
				// new InputStreamReader(mimePart.getInputStream()));
				//
				// for (String line; (line = in.readLine()) != null;)
				// System.out.println(" " + line);
				// }
				if (mimePart.isMimeType("APPLICATION/ZIP")) {
					InputStream in = mimePart.getInputStream();
					ZipInputStream zipIn = new ZipInputStream(in);
					System.out.println("Found ZIP");
					ZipEntry entry;
					while (null != (entry = zipIn.getNextEntry())) {
						System.out.println(" Dateiname: " + entry.getName());
					}
				}
			}
		}

		System.out.println("PPQ: " + subject);
		return false;
	}

	@Override
	public boolean maintenancePerformed(String gcNumber, Message message,
			String subject, String text) {
		System.out.println("performed Maintenance " + gcNumber);
		return false;
	}

}
