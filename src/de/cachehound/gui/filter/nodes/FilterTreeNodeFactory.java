package de.cachehound.gui.filter.nodes;

import de.cachehound.filter.AndFilter;
import de.cachehound.filter.IFilter;
import de.cachehound.filter.NotFilter;
import de.cachehound.filter.OrFilter;
import de.cachehound.filter.SimpleFilter;

public class FilterTreeNodeFactory {
	public AbstractFilterTreeNode doCreate(IFilter f) {
		if (f instanceof AndFilter) {
			AbstractFilterTreeNode node = new AndFilterTreeNode();
			for (IFilter childFilter : (AndFilter)f) {
				node.add(doCreate(childFilter));
			}
			return node;
		} else if (f instanceof OrFilter) {
			AbstractFilterTreeNode node = new OrFilterTreeNode();
			for (IFilter childFilter : (OrFilter)f) {
				node.add(doCreate(childFilter));
			}
			return node;
		} else if (f instanceof NotFilter) {
			AbstractFilterTreeNode node = new NotFilterTreeNode();
			node.add(doCreate(((NotFilter)f).getChild()));
			return node;
		} else {
			return new SimpleFilterTreeNode((SimpleFilter)f);
		}
	}
}
