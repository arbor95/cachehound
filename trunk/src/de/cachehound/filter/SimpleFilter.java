package de.cachehound.filter;


/**
 * Ein Filter, der nicht auf andere Filter zurueckgreift. Alle Unterklassen
 * sollten immutable sein.
 */
public abstract class SimpleFilter implements IFilter {
	@Override
	public SimpleFilter clone() {
		// Immutable, also brauchen wir keine Kopie zu erstellen.
		return this;
	}
}
