package de.cachehound.types;

import CacheWolf.beans.Global;

public enum LogType {

	PUBLISH {
		public String toIconString() {
			return "icon_greenlight.gif";
		}

		public String toGcComType() {
			return "Publish Listing";
		}
	},
	FOUND {
		public String toIconString() {
			return "icon_smile.gif";
		}

		public String toGcComType() {
			return "Found it";
		}
	},
	DID_NOT_FOUND {
		public String toIconString() {
			return "icon_sad.gif";
		}

		@Override
		public String toGcComType() {
			return "Didn't find it";
		}
	},
	MAINTENANCE_DONE {
		public String toIconString() {
			return "icon_maint.gif";
		}

		@Override
		public String toGcComType() {
			return "Owner Maintenance";
		}
	},

	NOTE {
		public String toIconString() {
			return "icon_note.gif";
		}

		@Override
		public String toGcComType() {
			return "Write note";
		}
	},
	REVIEWER_NOTE {
		public String toIconString() {
			return "big_smile.gif";
		}

		@Override
		public String toGcComType() {
			return "Post Reviewer Note";
		}
	},
	ENABLE_LISTING {
		public String toIconString() {
			return "icon_enabled.gif";
		}

		public String toGcComType() {
			return "Enable Listing";
		}
	},
	DISABLE_LISTING {
		public String toIconString() {
			return "icon_disabled.gif";
		}

		public String toGcComType() {
			return "Temporarily Disable Listing";
		}
	},
	NEEDS_MAINTENANCE {
		public String toIconString() {
			return "icon_needsmaint.gif";
		}

		public String toGcComType() {
			return "Needs Maintenance";
		}
	},
	NEEDS_ARCHIVED {
		public String toIconString() {
			return "icon_remove.gif";
		}

		@Override
		public String toGcComType() {
			return "Needs Archived";
		}
	},
	ARCHIVE {
		public String toIconString() {
			return "traffic_cone.gif";
		}

		@Override
		public String toGcComType() {
			return "Archive";
		}
	},
	UNARCHIVE {
		public String toIconString() {
			return "traffic_cone.gif";
		}

		@Override
		public String toGcComType() {
			return "Unarchive";
		}
	},
	RETRACT {
		public String toIconString() {
			return "img_redlight.gif";
		}

		@Override
		public String toGcComType() {
			return "Retract Listing";
		}
	},
	PHOTO_TAKEN {
		public String toIconString() {
			return "icon_camera.gif";
		}

		@Override
		public String toGcComType() {
			return "Webcam Photo Taken";
		}
	},
	WILL_ATTEND {
		public String toIconString() {
			return "icon_rsvp.gif";
		}

		@Override
		public String toGcComType() {
			return "Will Attend";
		}
	},
	ATTENDED {
		public String toIconString() {
			return "icon_attended.gif";
		}

		@Override
		public String toGcComType() {
			return "Attended";
		}
	},
	UPDATE_COORDINATES {
		public String toIconString() {
			return "coord_update.gif";
		}

		@Override
		public String toGcComType() {
			return "Update Coordinates";
		}
	},
	UNKNOWN {
		public String toIconString() {
			return "unknown Type";
		}

		@Override
		public String toGcComType() {
			return "unknown Type";
		}
	};

	public abstract String toIconString();

	public abstract String toGcComType();

	public static LogType getLogTypeFromIconString(String image) {
		if (image.equals("icon_smile.gif"))
			return LogType.FOUND;
		if (image.equals("icon_sad.gif"))
			return LogType.DID_NOT_FOUND;
		if (image.equals("icon_note.gif"))
			return LogType.NOTE;
		if (image.equals("icon_enabled.gif"))
			return LogType.ENABLE_LISTING;
		if (image.equals("icon_disabled.gif"))
			return LogType.DISABLE_LISTING;
		if (image.equals("icon_camera.gif"))
			return LogType.PHOTO_TAKEN;
		// TODO: eigentlich muss das dringend raus ....
		if (image.equals("11.png"))
			return LogType.PHOTO_TAKEN;
		if (image.equals("icon_attended.gif"))
			return LogType.ATTENDED;
		if (image.equals("icon_greenlight.gif"))
			return LogType.PUBLISH;
		if (image.equals("icon_rsvp.gif"))
			return LogType.WILL_ATTEND;
		if (image.equals("big_smile.gif"))
			return LogType.REVIEWER_NOTE;
		if (image.equals("traffic_cone.gif"))
			// TODO: Naja, laut Abbildung nicht eindeutig, könnte auch
			// unarchive sein
			return LogType.ARCHIVE;
		// TODO schon wieder eine nicht eindeutige abbildung
		if (image.equals("icon_maint.gif"))
			return LogType.NEEDS_MAINTENANCE;
		if (image.equals("icon_needsmaint.gif"))
			return LogType.NEEDS_MAINTENANCE;
		if (image.equals("coord_update.gif"))
			return LogType.UPDATE_COORDINATES;
		if (image.equals("icon_remove.gif"))
			return LogType.NEEDS_ARCHIVED;
		// TODO: Fehler loggen
		throw new RuntimeException("Fehler bei der Umwandlung der Logtypes");
	}

	public static LogType getLogTypeFromGcTypeText(String typeText) {
		if (typeText.equals("Found it") || typeText.equals("Found")
				|| typeText.equals("find"))
			return LogType.FOUND;
		if (typeText.equals("Didn't find it") || typeText.equals("Not Found")
				|| typeText.equals("no_find"))
			return LogType.DID_NOT_FOUND;
		if (typeText.equals("Write note") || typeText.equals("Note")
				|| typeText.equals("note") || typeText.equals("Not Attempted")
				|| typeText.equals("Other"))
			return LogType.NOTE;
		if (typeText.equals("Enable Listing"))
			return LogType.ENABLE_LISTING;
		if (typeText.equals("Temporarily Disable Listing"))
			return LogType.DISABLE_LISTING;
		if (typeText.equals("Webcam Photo Taken"))
			return LogType.PHOTO_TAKEN;
		if (typeText.equals("Attended"))
			return LogType.ATTENDED;
		if (typeText.equals("Publish Listing"))
			return LogType.PUBLISH;
		if (typeText.equals("Will Attend"))
			return LogType.WILL_ATTEND;
		if (typeText.equals("Post Reviewer Note"))
			return LogType.REVIEWER_NOTE;
		if (typeText.equals("Unarchive"))
			return LogType.UNARCHIVE;
		if (typeText.equals("Archive"))
			return LogType.ARCHIVE;
		if (typeText.equals("Owner Maintenance"))
			return LogType.MAINTENANCE_DONE;
		if (typeText.equals("Needs Maintenance"))
			return LogType.NEEDS_MAINTENANCE;
		if (typeText.equals("Needs Archived"))
			return LogType.NEEDS_ARCHIVED;
		if (typeText.equals("Update Coordinates"))
			return LogType.UPDATE_COORDINATES;
		if (typeText.equals("Retract Listing"))
			return LogType.RETRACT;
		Global.getPref().log(
				"GPX Import: warning, unknown logtype " + typeText
						+ " assuming Write note");
		return LogType.UNKNOWN;
	}

	// Diese beiden alten Methoden sind zu Dokumentationszwecken hier stehen
	// gelassen worden.

	// public static String image2TypeText(String image) {
	// if (image.equals("icon_smile.gif"))
	// return "Found it";
	// if (image.equals("icon_sad.gif"))
	// return "Didn't find it";
	// if (image.equals("icon_note.gif"))
	// return "Write note";
	// if (image.equals("icon_enabled.gif"))
	// return "Enable Listing";
	// if (image.equals("icon_disabled.gif"))
	// return "Temporarily Disable Listing";
	// if (image.equals("icon_camera.gif"))
	// return "Webcam Photo Taken";
	// if (image.equals("11.png"))
	// return "Webcam Photo Taken";
	// if (image.equals("icon_attended.gif"))
	// return "Attended";
	// if (image.equals("icon_greenlight.gif"))
	// return "Publish Listing";
	// if (image.equals("icon_rsvp.gif"))
	// return "Will Attend";
	// if (image.equals("big_smile.gif"))
	// return "Post Reviewer Note";
	// if (image.equals("traffic_cone.gif"))
	// return "Archive (show)";
	// if (image.equals("icon_maint.gif"))
	// return "Owner Maintenance";
	// if (image.equals("icon_needsmaint.gif"))
	// return "Needs Maintenance";
	// if (image.equals("coord_update.gif"))
	// return "Update Coordinates";
	// if (image.equals("icon_remove.gif"))
	// return "Needs Archived";
	// // TODO: Fehler loggen
	// return image;
	// }

	// if you change any of these make sure to check image2TypeText in the GPX
	// exporters
	// public static String typeText2Image(String typeText) {
	// if (typeText.equals("Found it") || typeText.equals("Found")
	// || typeText.equals("find"))
	// return "icon_smile.gif";
	// if (typeText.equals("Didn't find it") || typeText.equals("Not Found")
	// || typeText.equals("no_find"))
	// return "icon_sad.gif";
	// if (typeText.equals("Write note") || typeText.equals("Note")
	// || typeText.equals("note") || typeText.equals("Not Attempted")
	// || typeText.equals("Other"))
	// return "icon_note.gif";
	// if (typeText.equals("Enable Listing"))
	// return "icon_enabled.gif";
	// if (typeText.equals("Temporarily Disable Listing"))
	// return "icon_disabled.gif";
	// if (typeText.equals("Webcam Photo Taken"))
	// return "icon_camera.gif";
	// if (typeText.equals("Attended"))
	// return "icon_attended.gif";
	// if (typeText.equals("Publish Listing"))
	// return "icon_greenlight.gif";
	// if (typeText.equals("Will Attend"))
	// return "icon_rsvp.gif";
	// if (typeText.equals("Post Reviewer Note"))
	// return "big_smile.gif";
	// if (typeText.equals("Unarchive"))
	// return "traffic_cone.gif";
	// if (typeText.equals("Archive"))
	// return "traffic_cone.gif";
	// if (typeText.equals("Owner Maintenance"))
	// return "icon_maint.gif";
	// if (typeText.equals("Needs Maintenance"))
	// return "icon_needsmaint.gif";
	// if (typeText.equals("Needs Archived"))
	// return "icon_remove.gif";
	// if (typeText.equals("Update Coordinates"))
	// return "coord_update.gif";
	// if (typeText.equals("Retract Listing"))
	// return "img_redlight.gif";
	// Global.getPref().log(
	// "GPX Import: warning, unknown logtype " + typeText
	// + " assuming Write note");
	// return "icon_note.gif";
	// }

}
