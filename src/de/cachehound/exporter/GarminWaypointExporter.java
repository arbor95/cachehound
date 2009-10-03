package de.cachehound.exporter;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;

import CacheWolf.Global;
import de.cachehound.beans.ICacheHolder;
import de.cachehound.exporter.xml.LocDecoratorAddDT;
import de.cachehound.exporter.xml.LocDecoratorChangeType;
import de.cachehound.exporter.xml.LocExporter;
import de.cachehound.filter.AndFilter;
import de.cachehound.filter.CacheTypeFilter;
import de.cachehound.filter.FilterHelper;
import de.cachehound.filter.FoundFilter;
import de.cachehound.filter.HasCoordinatesFilter;
import de.cachehound.filter.IFilter;
import de.cachehound.types.CacheType;
import de.cachehound.util.gpsbabel.GPSBabel;
import de.cachehound.util.gpsbabel.GarminDevice;
import de.cachehound.util.gpsbabel.LocFile;

public class GarminWaypointExporter {
	/**
	 * Testet, ob die Voraussetzungen für den Exporter gegeben sind. Wenn nicht,
	 * kann die GUI den Exporter z.B. deaktiviert anzeigen.
	 * 
	 * Voraussetzungen in diesem Fall sind: GPSBabel installiert.
	 */
	public static boolean isActive() {
		return GPSBabel.isPresent();
	}

	private Map<IFilter, String> getMappings() {
		// FIXME: Make this configurable
		LinkedHashMap<IFilter, String> mappings = new LinkedHashMap<IFilter, String>();

		EnumSet<CacheType> tradis = EnumSet.of(CacheType.TRADITIONAL);
		IFilter tradiFilter = new CacheTypeFilter(tradis);
		mappings.put(new AndFilter(new FoundFilter(), tradiFilter), "Custom 1");
		mappings.put(tradiFilter, "Custom 0");

		EnumSet<CacheType> multis = EnumSet.of(CacheType.MULTI);
		IFilter multiFilter = new CacheTypeFilter(multis);
		mappings.put(new AndFilter(new FoundFilter(), multiFilter), "Custom 3");
		mappings.put(multiFilter, "Custom 2");

		EnumSet<CacheType> letterboxes = EnumSet.of(CacheType.LETTERBOX);
		IFilter letterboxFilter = new CacheTypeFilter(letterboxes);
		mappings.put(new AndFilter(new FoundFilter(), letterboxFilter),
				"Custom 5");
		mappings.put(letterboxFilter, "Custom 4");

		EnumSet<CacheType> mysteries = EnumSet.of(CacheType.UNKNOWN);
		IFilter mysteriesFilter = new CacheTypeFilter(mysteries);
		mappings.put(new AndFilter(new FoundFilter(), mysteriesFilter),
				"Custom 7");
		mappings.put(mysteriesFilter, "Custom 6");

		EnumSet<CacheType> events = EnumSet.of(CacheType.EVENT, CacheType.MEGA_EVENT);
		IFilter eventsFilter = new CacheTypeFilter(events);
		mappings.put(new AndFilter(new FoundFilter(), eventsFilter),
				"Custom 9");
		mappings.put(eventsFilter, "Custom 8");

		EnumSet<CacheType> citos = EnumSet.of(CacheType.CITO);
		IFilter citossFilter = new CacheTypeFilter(citos);
		mappings.put(new AndFilter(new FoundFilter(), citossFilter),
				"Custom 11");
		mappings.put(citossFilter, "Custom 10");

		EnumSet<CacheType> virtuals = EnumSet.of(CacheType.VIRTUAL);
		IFilter vitualsFilter = new CacheTypeFilter(virtuals);
		mappings.put(vitualsFilter, "Custom 12");

		EnumSet<CacheType> webcams = EnumSet.of(CacheType.WEBCAM);
		IFilter webcamsFilter = new CacheTypeFilter(webcams);
		mappings.put(webcamsFilter, "Custom 13");

		EnumSet<CacheType> locationless = EnumSet.of(CacheType.LOCATIONLESS);
		IFilter locationlessFilter = new CacheTypeFilter(locationless);
		mappings.put(locationlessFilter, "Custom 14");

		EnumSet<CacheType> earthcaches = EnumSet.of(CacheType.EARTH);
		IFilter earthcachesFilter = new CacheTypeFilter(earthcaches);
		mappings.put(earthcachesFilter, "Custom 15");

		EnumSet<CacheType> finals = EnumSet.of(CacheType.FINAL);
		IFilter finalsFilter = new CacheTypeFilter(finals);
		mappings.put(finalsFilter, "Custom 16");

		EnumSet<CacheType> parkings = EnumSet.of(CacheType.PARKING);
		IFilter parkingFilter = new CacheTypeFilter(parkings);
		mappings.put(parkingFilter, "Custom 17");

		EnumSet<CacheType> qtas = EnumSet.of(CacheType.QUESTION);
		IFilter qtasFilter = new CacheTypeFilter(qtas);
		mappings.put(qtasFilter, "Custom 18");

		EnumSet<CacheType> soams = EnumSet.of(CacheType.STAGE);
		IFilter soamsFilter = new CacheTypeFilter(soams);
		mappings.put(soamsFilter, "Custom 19");

		EnumSet<CacheType> trailheads = EnumSet.of(CacheType.TRAILHEAD);
		IFilter trailheadsFilter = new CacheTypeFilter(trailheads);
		mappings.put(trailheadsFilter, "Custom 20");

		EnumSet<CacheType> refpoints = EnumSet.of(CacheType.REFERENCE);
		IFilter refpointsFilter = new CacheTypeFilter(refpoints);
		mappings.put(refpointsFilter, "Custom 21");

		return mappings;
	}

	/**
	 * Schiebt Caches als Waypoints aufs Garmin (Etrex-Serie und 60er-Serie)
	 * 
	 * @throws IOException
	 */
	public void doit(Collection<? extends ICacheHolder> caches)
			throws IOException {
		File temp = File.createTempFile("ch-", ".loc");
		temp.deleteOnExit();

		LocExporter exp = new LocExporter(temp);

		exp.addDecorator(new LocDecoratorAddDT());
		exp.addDecorator(new LocDecoratorChangeType(getMappings()));

		exp.doit(FilterHelper.applyFilter(new HasCoordinatesFilter(), caches));

		GPSBabel.convert(new LocFile(temp), new GarminDevice(
				Global.getPref().garminConn + ":"));
	}
}
