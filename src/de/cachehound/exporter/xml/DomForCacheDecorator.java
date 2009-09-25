package de.cachehound.exporter.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import de.cachehound.beans.ICacheHolder;

public abstract class DomForCacheDecorator implements IDomForCache {
	protected DomForCacheDecorator(IDomForCache decoratee) {
		this.decoratee = decoratee;
	}
	
	@Override
	public Document getDomForCache(ICacheHolder cache) {
		return decoratee.getDomForCache(cache);
	}
	
	private IDomForCache decoratee;
}
