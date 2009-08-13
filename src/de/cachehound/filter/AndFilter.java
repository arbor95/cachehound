package de.cachehound.filter;

import de.cachehound.beans.ICacheHolder;

public class AndFilter extends ListFilter {

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
//		return "AND";
	}
}
