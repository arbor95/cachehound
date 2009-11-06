package de.cachehound.types;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum CacheType {

	/** custom waypoint */
	CUSTOM((byte) 0, "typeCustom.png", "Custom", "Custum", "C", false, false,
			"no Oc Id", "no GC Website ID"),
	/** traditional cache (GC,OC) */
	TRADITIONAL((byte) 2, "typeTradi.png", "Traditional", "Traditional Cache",
			"T", false, true, "2", "2"),
	/** multi cache (GC,OC) */
	MULTI((byte) 3, "typeMulti.png", "Multi", "Multi-cache", "M", false, true,
			"3", "3"),
	/** virtual cache (GC) */
	VIRTUAL((byte) 4, "typeVirtual.png", "Virtual", "Virtual Cache", "V",
			false, true, "4", "4"),
	/** letterbox cache (GC) */
	LETTERBOX((byte) 5, "typeLetterbox.png", "Letterbox", "Letterbox Hybrid",
			"L", false, true, "no Oc Id", "5"),
	/** event cache (OC,GC) */
	EVENT((byte) 6, "typeEvent.png", "Event", "Event Cache", "X", false, true,
			"6", "6"),
	/** unknown cache (GC) */
	UNKNOWN((byte) 8, "typeUnknown.png", "Mystery", "Unknown Cache", "U",
			false, true, "1", "8"),
	/** quiz cache (OC) */
	QUIZ((byte) 7, "typeUnknown.png", "Quiz", "Unknown Cache", "U", false,
			true, "7", "no GC Website ID"),
	/** math cache (OC) */
	MATH((byte) 108, "typeMath.png", "Math", "Unknown Cache", "U", false, true,
			"8", "no GC Website ID"),
	/** moving cache (OC) */
	MOVING((byte) 9, "typeMoving.png", "Moving", "Unknown Cache", "U", false,
			true, "9", "no GC Website ID"),
	/** drive in cache (OC) */
	DRIVE_IN((byte) 10, "typeDrivein.png", "Drive in", "Traditional Cache",
			"T", false, true, "10", "no GC Website ID"),
	/** webcam cache (GC,OC) */
	WEBCAM((byte) 11, "typeWebcam.png", "Webcam", "Webcam Cache", "W", false,
			true, "5", "11"),
	/** locationless cache (GC) */
	LOCATIONLESS((byte) 12, "typeLocless.png", "Locationless",
			"Locationless (Reverse) Cache", "O", false, true, "4", "12"),
	/** CITO cache (GC,OC) */
	CITO((byte) 13, "typeCito.png", "CITO", "Cache In Trash Out Event", "X",
			false, true, "no Oc Id", "13"),
	/** Additional Waypoint Parking (GC) */
	PARKING((byte) 50, "typeParking.png", "Addi: Parking", "Parking Area", "P",
			true, false, "no Oc Id", "no GC Website ID"),
	/** Additional Waypoint Stage of a Multi (GC) */
	STAGE((byte) 51, "typeStage.png", "Addi: Stage", "Stages of a Multicache",
			"S", true, false, "no Oc Id", "no GC Website ID"),
	/** Additional Waypoint Question to answer (GC) */
	QUESTION((byte) 52, "typeQuestion.png", "Addi: Question",
			"Question to Answer", "Q", true, false, "no Oc Id",
			"no GC Website ID"),
	/** Additional Waypoint Final (GC) */
	FINAL((byte) 53, "typeFinal.png", "Addi: Final", "Final Location", "F",
			true, false, "no Oc Id", "no GC Website ID"),
			// Old, but the new one should be correct (for Website and gpx-files)
	// FINAL((byte) 53, "typeFinal.png", "Addi: Final",
	// "Final Coordinates", "F", true, false, "no Oc Id", "no GC Website ID"),
	/** Additional Waypoint Trailhead (GC) */
	TRAILHEAD((byte) 54, "typeTrailhead.png", "Addi: Trailhead", "Trailhead",
			"H", true, false, "no Oc Id", "no GC Website ID"),
	/** Additional Waypoint Reference (GC) */
	REFERENCE((byte) 55, "typeReference.png", "Addi: Reference",
			"Reference Point", "R", true, false, "no Oc Id", "no GC Website ID"),
	/** Mega Event Cache (GC) */
	MEGA_EVENT((byte) 100, "typeMegaevent.png", "Mega Event",
			"Mega-Event Cache", "X", false, true, "no Oc Id", "453"),
	/** WhereIGo Cache (GC) */
	WHEREIGO((byte) 101, "typeWhereigo.png", "WhereIGo", "Wherigo Cache", "G",
			false, true, "no Oc Id", "1858"),
	/** Project Ape cache (GC) */
	APE((byte) 102, "typeApe.png", "Project Ape", "Project APE Cache", "T",
			false, true, "no Oc Id", "9"),
	/** Adenture Maze Exhibit (GC) */
	MAZE((byte) 103, "typeMaze.png", "Maze Exhibit", "GPS Adventures Exhibit",
			"X", false, true, "no Oc Id", "1304"),
	/** Earth Cache (GC) */
	EARTH((byte) 104, "typeEarth.png", "Earchcache", "Earthcache", "E", false,
			true, "no Oc Id", "137"),
	/**
	 * unrecognized cache type or missing information, should throw
	 * IllegalArgumentExceptions when found
	 */
	// Should always be the last Element of this Enum
	ERROR((byte) -1, "Error", "Error", "Error", "Error", false, false, "Error",
			"Error");

	private static Logger logger = LoggerFactory.getLogger(CacheType.class);

	private byte oldCWByte;
	private String guiImage;
	private String guiString;
	private String gcGpxString;
	private String shortExportString;
	private String ocId;
	private String gcWebsiteId;

	private boolean additionalWaypoint;
	private boolean cacheWaypoint;

	private CacheType(byte oldCWByte, String guiImage, String guiString,
			String gcGpxString, String shortExportString,
			boolean additionalWaypoint, boolean cacheWaypoint, String ocId,
			String gcWebsiteId) {
		this.oldCWByte = oldCWByte;
		this.guiImage = guiImage;
		this.guiString = guiString;
		this.gcGpxString = gcGpxString;
		this.shortExportString = shortExportString;
		this.additionalWaypoint = additionalWaypoint;
		this.cacheWaypoint = cacheWaypoint;
		this.ocId = ocId;
		this.gcWebsiteId = gcWebsiteId;
	}

	public String getShortExport() {
		return shortExportString;
	}

	public byte getOldCWByte() {
		return oldCWByte;
	}

	public String getGuiImage() {
		return guiImage;
	}

	public String getGcGpxString() {
		return gcGpxString;
	}

	public boolean isAdditionalWaypoint() {
		return additionalWaypoint;
	}

	public boolean isCacheWaypoint() {
		return cacheWaypoint;
	}

	public String getGuiString() {
		return guiString;
	}

	public static CacheType fromGuiString(String guiString) {
		for (CacheType type : values()) {
			if (type.guiString.equals(guiString)) {
				return type;
			}
		}
		logger.error("Get unknown GuiString for converting: {}", guiString);
		throw new IllegalArgumentException("unmatched argument " + guiString);
	}

	public static CacheType fromGcGpxString(String gcGpxString) {
		String typeString;
		if (gcGpxString.startsWith("Geocache|")) {
			typeString = gcGpxString.substring(9);
		} else if (gcGpxString.startsWith("Waypoint|")) {
			typeString = gcGpxString.substring(9);
		} else {
			typeString = gcGpxString;
		}
		for (CacheType type : values()) {
			if (type.gcGpxString.equals(typeString)) {
				return type;
			}
		}
		logger.error("Get unknown GcGpxString for converting: {}", gcGpxString);
		throw new IllegalArgumentException("unmatched argument " + gcGpxString);
	}

	public static CacheType fromOcTypeId(String ocTypeId) {
		for (CacheType type : values()) {
			if (type.ocId.equals(ocTypeId)) {
				return type;
			}
		}
		logger.error("Get unknown OcTypeId for converting: {}", ocTypeId);
		throw new IllegalArgumentException("unmatched argument " + ocTypeId);
	}

	public static CacheType fromGcWebsiteId(String gcWebsiteId) {
		for (CacheType type : values()) {
			if (type.gcWebsiteId.equals(gcWebsiteId)) {
				return type;
			}
		}
		logger.error("Get unknown GcWebsiteId for converting: {}", gcWebsiteId);
		throw new IllegalArgumentException("unmatched argument " + gcWebsiteId);
	}

	public static CacheType fromOldCWByte(byte oldCWByte) {
		for (CacheType type : values()) {
			if (type.oldCWByte == oldCWByte) {
				return type;
			}
		}
		logger.error("Get unknown OldCWByte for converting: {}", oldCWByte);
		throw new IllegalArgumentException("unmatched argument " + oldCWByte);
	}
}
