package de.cachehound.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.jdom.Element;

public abstract class ListFilter implements Iterable<IFilter>, IFilter {
	private List<IFilter> list;

	public ListFilter(IFilter... filters) {
		this(Arrays.asList(filters));
	}
	
	public ListFilter(Collection<? extends IFilter> filters) {
		this.list = new ArrayList<IFilter>(filters);
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
	
	@Override
	public Element toXML() {
		Element ret = new Element(xmlElementName());
		
		for (IFilter f : this) {
			ret.addContent(f.toXML());
		}
		
		return ret;
	}
	
	protected abstract String xmlElementName();
}
