package de.cachehound.filter;

import java.util.EnumSet;
import java.util.Set;

import org.jdom.Element;

import de.cachehound.beans.ICacheHolder;

/**
 * Abstrakte Basisklasse für Filter, die von genau einem CacheHolder-Attribut
 * abhängen. Dieses muss einen Enum-Typ haben.
 */
public abstract class AbstractEnumBasedFilter<T extends Enum<T>> extends
		SimpleFilter {
	private Set<T> mask;

	protected AbstractEnumBasedFilter(Set<T> mask) {
		this.mask = EnumSet.copyOf(mask);
	}

	protected abstract T getProperty(ICacheHolder ch);

	@Override
	public boolean cacheIsVisible(ICacheHolder ch) {
		return mask.contains(getProperty(ch));
	}

	public Set<T> getMask() {
		return EnumSet.copyOf(mask);
	}

	@Override
	public boolean equals(Object o) {
		return this.getClass() == o.getClass()
				&& this.getMask().equals(
						((AbstractEnumBasedFilter<?>) o).getMask());
	}

	@Override
	public int hashCode() {
		return this.getClass().hashCode() + this.getMask().hashCode();
	}

	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder();
		for (T b : getMask()) {
			ret.append(b.toString());
			ret.append(" ");
		}
		return ret.toString();
	}

	@Override
	public Element toXML() {
		Element ret = new Element(xmlElementName());

		for (T b : getMask()) {
			ret.addContent((new Element("enabled")).setText(b.toString()));
		}

		return ret;
	}

	protected abstract String xmlElementName();
}
