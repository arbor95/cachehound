package de.cachehound.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ListFilter implements Iterable<IFilter>, IFilter {
	private List<IFilter> list;

	private static Logger logger = LoggerFactory.getLogger(ListFilter.class);
	
	public ListFilter(IFilter... filters) {
		this(Arrays.asList(filters));
	}
	
	public ListFilter(Collection<? extends IFilter> filters) {
		this.list = new ArrayList<IFilter>(filters);
	}

	@Override
	public ListFilter clone() {
		try {
			ListFilter ret = (ListFilter) super.clone();
			ret.list = new ArrayList<IFilter>();
			for (IFilter f : this.list) {
				ret.list.add(f.clone());
			}
			return ret;
		} catch (CloneNotSupportedException e) {
			logger.error("Object.clone() threw CloneNotSupportedException", e);
			assert(false);
			return null;
		}
	}

	@Override
	public boolean equals(Object o) {
		return list.equals(o);
	}

	@Override
	public int hashCode() {
		return list.hashCode();
	}

	public Iterator<IFilter> iterator() {
		return Collections.unmodifiableList(list).iterator();
	}
}
