package de.cachehound.exporter;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;

import de.cachehound.beans.ICacheHolder;
import de.cachehound.exporter.xml.LocDecoratorAddDT;
import de.cachehound.exporter.xml.LocDecoratorChangeType;
import de.cachehound.exporter.xml.LocExporter;
import de.cachehound.filter.AndFilter;
import de.cachehound.filter.CacheTypeFilter;
import de.cachehound.filter.FoundFilter;
import de.cachehound.filter.IFilter;
import de.cachehound.types.CacheType;
import de.cachehound.util.GPSBabel;

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
		mappings.put(new AndFilter(new FoundFilter(), new CacheTypeFilter(
				tradis)), "Custom 1");
		mappings.put(new CacheTypeFilter(tradis), "Custom 0");
		EnumSet<CacheType> multis = EnumSet.of(CacheType.MULTI);
		mappings.put(new AndFilter(new FoundFilter(), new CacheTypeFilter(
				multis)), "Custom 3");
		mappings.put(new CacheTypeFilter(multis), "Custom 2");

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
	}
}
