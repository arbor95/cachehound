package de.cachehound.types;

public enum CacheType {

	/** custom waypoint */
	CUSTOM((byte) 0, "typeCustom.png", "Custom", "Custum"),
	/** traditional cache (GC,OC) */
	TRADITIONAL((byte) 2, "typeTradi.png", "Traditional", "Traditional Cache"),
	/** multi cache (GC,OC) */
	MULTI((byte) 3, "typeMulti.png", "Multi", "Multi-cache"),
	/** virtual cache (GC) */
	VIRTUAL((byte) 4, "typeVirtual.png", "Virtual", "Virtual Cache"),
	/** letterbox cache (GC) */
	LETTERBOX((byte) 5, "typeLetterbox.png", "Letterbox", "Letterbox Hybrid"),
	/** event cache (OC,GC) */
	EVENT((byte) 6, "typeEvent.png", "Event", "Event Cache"),
	/** quiz cache (OC) */
	QUIZ((byte) 7, "typeUnknown.png", "Mystery", "Unknown Cache"),
	/** unknown cache (GC) */
	UNKNOWN((byte) 8, "typeUnknown.png", "Mystery", "Unknown Cache"),
	/** math cache (OC) */
	MATH((byte) 108, "typeMath.png", "Math", "Unknown Cache"),
	/** moving cache (OC) */
	MOVING((byte) 9, "typeMoving.png", "Moving", "Unknown Cache"),
	/** drive in cache (OC) */
	DRIVE_IN((byte) 10, "typeDrivein.png", "Drive in", "Traditional Cache"),
	/** webcam cache (GC,OC) */
	WEBCAM((byte) 11, "typeWebcam.png", "Webcam", "Webcam Cache" ),
	/** locationless cache (GC) */
	LOCATIONLESS((byte) 12, "typeLocless.png", "Locationless", "Locationless (Reverse) Cache"),
	/** CITO cache (GC,OC) */
	CITO((byte) 13, "typeCito.png", "CITO",  "Cache In Trash Out Event"),
	/** Additional Waypoint Parking (GC) */
	PARKING((byte) 50, "typeParking.png", "Addi: Parking", "Waypoint|Parking Area"),
	/** Additional Waypoint Stage of a Multi (GC) */
	STAGE((byte) 51, "typeStage.png", "Addi: Stage", "Waypoint|Stages of a Multicache"),
	/** Additional Waypoint Question to answer (GC) */
	QUESTION((byte) 52, "typeQuestion.png", "Addi: Question", "Waypoint|Question to Answer"),
	/** Additional Waypoint Final (GC) */
	FINAL((byte) 53, "typeFinal.png", "Addi: Final", "Waypoint|Final Coordinates"),
	/** Additional Waypoint Trailhead (GC) */
	TRAILHEAD((byte) 54, "typeTrailhead.png", "Addi: Trailhead", "Waypoint|Trailhead"),
	/** Additional Waypoint Reference (GC) */
	REFERENCE((byte) 55, "typeReference.png", "Addi: Reference", "Waypoint|Reference Point"),
	/** Mega Event Cache (GC) */
	MEGA_EVENT((byte) 100, "typeMegaevent.png", "Mega Event", "Mega-Event Cache"),
	/** WhereIGo Cache (GC) */
	WHEREIGO((byte) 101, "typeWhereigo.png", "WhereIGo", "Wherigo Cache"),
	/** Project Ape cache (GC) */
	APE((byte) 102, "typeApe.png", "Project Ape", "Project APE Cache"),
	/** Adenture Maze Exhibit (GC) */
	MAZE((byte) 103, "typeMaze.png", "Maze Exhibit", "GPS Adventures Exhibit"),
	/** Earth Cache (GC) */
	EARTH((byte) 104, "typeEarth.png", "Earchcache", "Earthcache"),
	/**
	 * unrecognized cache type or missing information, should throw
	 * IllegalArgumentExceptions when found
	 */
	ERROR((byte) -1, null, null, null);

	private byte oldCWByte;
	private String guiImage;
	private String guiString;
	private String gcGpxString;
	
	private CacheType(byte oldCWByte, String guiImage, String guiString, String gcGpxString) {
		this.oldCWByte = oldCWByte;
		this.guiImage = guiImage;
		this.guiString = guiString;
		this.gcGpxString = gcGpxString;
	}

}
