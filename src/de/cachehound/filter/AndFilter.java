package de.cachehound.filter;

import java.util.ArrayList;

import de.cachehound.beans.ICacheHolder;

public class AndFilter extends ArrayList<IFilter> implements IFilter {

	@Override
	public boolean cacheIsVisible(ICacheHolder ch) {
		for (IFilter f : this) {
			if (!f.cacheIsVisible(ch)) {
				return false;
			}
		}
		return true;
	}

}
