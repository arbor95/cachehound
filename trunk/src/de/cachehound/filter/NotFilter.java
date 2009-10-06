package de.cachehound.filter;

import org.jdom.Element;

import de.cachehound.beans.ICacheHolder;

public class NotFilter implements IFilter {
	private IFilter child;
	
	public NotFilter(IFilter filter) {
		this.child = filter;
	}
	
	public IFilter getChild() {
		return child;
	}

	@Override
	public NotFilter clone() {
		// Immutable, also brauchen wir keine Kopie zu erstellen.
		return this;
	}

	@Override
	public boolean cacheIsVisible(ICacheHolder ch) {
		return !child.cacheIsVisible(ch);
	}
	
	@Override
	public String toString() {
		return "NOT " + child.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((child == null) ? 0 : child.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NotFilter other = (NotFilter) obj;
		if (child == null) {
			if (other.child != null)
				return false;
		} else if (!child.equals(other.child))
			return false;
		return true;
	}

	@Override
	public Element toXML() {
		Element ret = new Element("not");
		
		ret.addContent(child.toXML());
		
		return ret;
	}
}
