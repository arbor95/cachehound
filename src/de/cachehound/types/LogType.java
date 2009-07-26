package de.cachehound.types;

import CacheWolf.Global;

public enum LogType {
	publish {
		public String toIconString() {
			return "icon_greenlight.gif";
		}
	},
	found {
		public String toIconString() {
			return "icon_smile.gif";
		}
	},
	didNotFound {
		public String toIconString() {
			return "icon_sad.gif";
		}
	},
	maintenanceDone {
		public String toIconString() {
			return "icon_maint.gif";
		}
	},
	note {
		public String toIconString() {
			return "icon_note.gif";
		}
	},
	reviewerNote {
		public String toIconString() {
			return "big_smile.gif";
		}
	},
	enableListing {
		public String toIconString() {
			return "icon_enabled.gif";
		}
	},
	disableListing {
		public String toIconString() {
			return "icon_disabled.gif";
		}
	},
	needsMaintenance {
		public String toIconString() {
			return "icon_needsmaint.gif";
		}
	},
	needsArchived {
		public String toIconString() {
			return "icon_remove.gif";
		}
	},
	archive {
		public String toIconString() {
			return "traffic_cone.gif";
		}
	},
	unarchive {
		public String toIconString() {
			return "traffic_cone.gif";
		}
	},
	retract {
		public String toIconString() {
			return "img_redlight.gif";
		}
	},
	photoTaken {
		public String toIconString() {
			return "icon_camera.gif";
		}
	},
	willAttend {
		public String toIconString() {
			return "icon_rsvp.gif";
		}
	},
	attended {
		public String toIconString() {
			return "icon_attended.gif";
		}
	},
	updatesCoordinates {
		public String toIconString() {
			return "coord_update.gif";
		}
	};

	public abstract String toIconString();

	public static LogType getLogTypeFromIconString(String image) {
		if (image.equals("icon_smile.gif"))
			return LogType.found;
		if (image.equals("icon_sad.gif"))
			return LogType.didNotFound;
		if (image.equals("icon_note.gif"))
			return LogType.note;
		if (image.equals("icon_enabled.gif"))
			return LogType.enableListing;
		if (image.equals("icon_disabled.gif"))
			return LogType.disableListing;
		if (image.equals("icon_camera.gif"))
			return LogType.photoTaken;
		// TODO: eigentlich muss das dringend raus ....
		if (image.equals("11.png"))
			return LogType.photoTaken;
		if (image.equals("icon_attended.gif"))
			return LogType.attended;
		if (image.equals("icon_greenlight.gif"))
			return LogType.publish;
		if (image.equals("icon_rsvp.gif"))
			return LogType.willAttend;
		if (image.equals("big_smile.gif"))
			return LogType.reviewerNote;
		if (image.equals("traffic_cone.gif"))
			// TODO: Naja, laut Abbildung nicht eindeutig, könnte auch
			// unarchive sein
			return LogType.archive;
		// TODO schon wieder eine nicht eindeutige abbildung
		if (image.equals("icon_maint.gif"))
			return LogType.needsMaintenance;
		if (image.equals("icon_needsmaint.gif"))
			return LogType.needsMaintenance;
		if (image.equals("coord_update.gif"))
			return LogType.updatesCoordinates;
		if (image.equals("icon_remove.gif"))
			return LogType.needsArchived;
		// TODO: Fehler loggen
		throw new RuntimeException("Fehler bei der Umwandlung der Logtypes");
	}

	// TODO: Entfernen dieser beiden alten Helfermethoden, da komplett auf die
	// enum umgestellt werden sollte.

	public static String image2TypeText(String image) {
		if (image.equals("icon_smile.gif"))
			return "Found it";
		if (image.equals("icon_sad.gif"))
			return "Didn't find it";
		if (image.equals("icon_note.gif"))
			return "Write note";
		if (image.equals("icon_enabled.gif"))
			return "Enable Listing";
		if (image.equals("icon_disabled.gif"))
			return "Temporarily Disable Listing";
		if (image.equals("icon_camera.gif"))
			return "Webcam Photo Taken";
		if (image.equals("11.png"))
			return "Webcam Photo Taken";
		if (image.equals("icon_attended.gif"))
			return "Attended";
		if (image.equals("icon_greenlight.gif"))
			return "Publish Listing";
		if (image.equals("icon_rsvp.gif"))
			return "Will Attend";
		if (image.equals("big_smile.gif"))
			return "Post Reviewer Note";
		if (image.equals("traffic_cone.gif"))
			return "Archive (show)";
		if (image.equals("icon_maint.gif"))
			return "Owner Maintenance";
		if (image.equals("icon_needsmaint.gif"))
			return "Needs Maintenance";
		if (image.equals("coord_update.gif"))
			return "Update Coordinates";
		if (image.equals("icon_remove.gif"))
			return "Needs Archived";
		// TODO: Fehler loggen
		return image;
	}

	// if you change any of these make sure to check image2TypeText in the GPX
	// exporters
	public static String typeText2Image(String typeText) {
		if (typeText.equals("Found it") || typeText.equals("Found")
				|| typeText.equals("find"))
			return "icon_smile.gif";
		if (typeText.equals("Didn't find it") || typeText.equals("Not Found")
				|| typeText.equals("no_find"))
			return "icon_sad.gif";
		if (typeText.equals("Write note") || typeText.equals("Note")
				|| typeText.equals("note") || typeText.equals("Not Attempted")
				|| typeText.equals("Other"))
			return "icon_note.gif";
		if (typeText.equals("Enable Listing"))
			return "icon_enabled.gif";
		if (typeText.equals("Temporarily Disable Listing"))
			return "icon_disabled.gif";
		if (typeText.equals("Webcam Photo Taken"))
			return "icon_camera.gif";
		if (typeText.equals("Attended"))
			return "icon_attended.gif";
		if (typeText.equals("Publish Listing"))
			return "icon_greenlight.gif";
		if (typeText.equals("Will Attend"))
			return "icon_rsvp.gif";
		if (typeText.equals("Post Reviewer Note"))
			return "big_smile.gif";
		if (typeText.equals("Unarchive"))
			return "traffic_cone.gif";
		if (typeText.equals("Archive"))
			return "traffic_cone.gif";
		if (typeText.equals("Owner Maintenance"))
			return "icon_maint.gif";
		if (typeText.equals("Needs Maintenance"))
			return "icon_needsmaint.gif";
		if (typeText.equals("Needs Archived"))
			return "icon_remove.gif";
		if (typeText.equals("Update Coordinates"))
			return "coord_update.gif";
		if (typeText.equals("Retract Listing"))
			return "img_redlight.gif";
		Global.getPref().log(
				"GPX Import: warning, unknown logtype " + typeText
						+ " assuming Write note");
		return "icon_note.gif";
	}

}
