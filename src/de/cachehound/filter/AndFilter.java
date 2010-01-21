package de.cachehound.filter;

import java.util.Collection;

import de.cachehound.beans.ICacheHolder;

public class AndFilter extends ListFilter {
	public AndFilter(IFilter... filters) {
		super(filters);
	}

	public AndFilter(Collection<? extends IFilter> filters) {
		super(filters);
	}

	@Override
	public boolean cacheIsVisible(ICacheHolder ch) {
		for (IFilter f : this) {
			if (!f.cacheIsVisible(ch)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder("AND [");
		for (IFilter f : this) {
			ret.append(f.toString());
			ret.append(" ");
		}
		ret.append("]");
		return ret.toString();
	}

	@Override
	protected String xmlElementName() {
		return "and";
	}
}
