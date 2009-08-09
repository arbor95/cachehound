package de.cachehound.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ListFilter implements List<IFilter>, IFilter {
	private List<IFilter> list = new ArrayList<IFilter>();

	private static Logger logger = LoggerFactory.getLogger(ListFilter.class);

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

	public boolean add(IFilter e) {
		return list.add(e);
	}

	public void add(int index, IFilter element) {
		list.add(index, element);
	}

	public boolean addAll(Collection<? extends IFilter> c) {
		return list.addAll(c);
	}

	public boolean addAll(int index, Collection<? extends IFilter> c) {
		return list.addAll(index, c);
	}

	public void clear() {
		list.clear();
	}

	public boolean contains(Object o) {
		return list.contains(o);
	}

	public boolean containsAll(Collection<?> c) {
		return list.containsAll(c);
	}

	public boolean equals(Object o) {
		return list.equals(o);
	}

	public IFilter get(int index) {
		return list.get(index);
	}

	public int hashCode() {
		return list.hashCode();
	}

	public int indexOf(Object o) {
		return list.indexOf(o);
	}

	public boolean isEmpty() {
		return list.isEmpty();
	}

	public Iterator<IFilter> iterator() {
		return list.iterator();
	}

	public int lastIndexOf(Object o) {
		return list.lastIndexOf(o);
	}

	public ListIterator<IFilter> listIterator() {
		return list.listIterator();
	}

	public ListIterator<IFilter> listIterator(int index) {
		return list.listIterator(index);
	}

	public IFilter remove(int index) {
		return list.remove(index);
	}

	public boolean remove(Object o) {
		return list.remove(o);
	}

	public boolean removeAll(Collection<?> c) {
		return list.removeAll(c);
	}

	public boolean retainAll(Collection<?> c) {
		return list.retainAll(c);
	}

	public IFilter set(int index, IFilter element) {
		return list.set(index, element);
	}

	public int size() {
		return list.size();
	}

	public List<IFilter> subList(int fromIndex, int toIndex) {
		return list.subList(fromIndex, toIndex);
	}

	public Object[] toArray() {
		return list.toArray();
	}

	public <T> T[] toArray(T[] a) {
		return list.toArray(a);
	}
}
