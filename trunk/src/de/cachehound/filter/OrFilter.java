package de.cachehound.filter;

import java.util.Collection;

import de.cachehound.beans.ICacheHolder;

public class OrFilter extends ListFilter {
	public OrFilter(IFilter... filters) {
		super(filters);
	}
	
	public OrFilter(Collection<? extends IFilter> filters) {
		super(filters);
	}

	@Override
	public boolean cacheIsVisible(ICacheHolder ch) {
		for (IFilter f : this) {
			if (f.cacheIsVisible(ch)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder("OR [");
		for (IFilter f : this) {
			ret.append(f.toString());
			ret.append(" ");
		}
		ret.append("]");
		return ret.toString();
//		return "OR";
	}
}
