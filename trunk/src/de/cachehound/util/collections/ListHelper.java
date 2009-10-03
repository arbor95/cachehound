package de.cachehound.util.collections;

import java.util.ArrayList;
import java.util.List;

public class ListHelper {
	/**
	 * Die uebergebene Liste wird in 2 Teile geteilt und eine neue Liste
	 * zurueckgegeben, in der der zweite Teil vor dem ersten ist. Die Trennung
	 * erfolgt am uebergebenen Element. Genauer: Das erste Vorkommen von elem
	 * ist das erste Element der neuen Liste.
	 */
	public static <E> List<E> divideAndSwap(List<E> l, E elem) {
		int index = l.indexOf(elem);

		List<E> ret = new ArrayList<E>();
		ret.addAll(l.subList(index, l.size()));
		ret.addAll(l.subList(0, index));

		return ret;
	}
}
