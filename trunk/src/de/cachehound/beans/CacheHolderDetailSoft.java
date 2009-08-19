package de.cachehound.beans;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import CacheWolf.Global;
import CacheWolf.beans.Attributes;
import CacheWolf.beans.CacheHolder;
import CacheWolf.beans.CacheImages;
import CacheWolf.beans.ImageInfo;
import CacheWolf.beans.TravelbugList;
import de.cachehound.factory.CacheHolderDetailFactory;

/**
 * Implementierung des {@code ICacheHolderDetail} Interfaces mithilfe von
 * {@code SoftReference}s. Nach aussen geschieht dies fast vollkommen
 * transparent: Beim Zugriff werden echte Objekte bei Bedarf von der Platte
 * gelesen. Bei Speichermangel werden unveränderte Objekte verworfen - geänderte
 * natürlich erst, nachtem save() aufgerufen wurde. Um OOM-Errors zu vermeiden,
 * sollte beim ändern von vielen Objekte (wie beim Import/Spidern) zeitnah
 * save() aufgerufen werden.
 * 
 * (Jaja, das wird noch geändert!)
 */
public class CacheHolderDetailSoft implements ICacheHolderDetail {
	private static Logger logger = LoggerFactory
			.getLogger(CacheHolderDetailSoft.class);

	private CacheHolder parent;
	private SoftReference<CacheHolderDetail> ref;

	// Map: GCCode -> CHDs
	private static Map<String, CacheHolderDetail> dirtyObjects = new HashMap<String, CacheHolderDetail>();

	public CacheHolderDetailSoft(CacheHolderDetail impl) {
		this.ref = new SoftReference<CacheHolderDetail>(impl);
		this.parent = impl.getParent();
	}

	private CacheHolderDetail getImpl() {
		if (dirtyObjects.containsKey(parent.getWayPoint())) {
			return dirtyObjects.get(parent.getWayPoint());
		}

		CacheHolderDetail impl = ref.get();
		if (impl != null) {
			return impl;
		}

		try {
			return CacheHolderDetailFactory.getInstance()
					.createCacheHolderDetailFromFile(parent,
							Global.getProfile().getDataDir());
		} catch (IOException e) {
			logger.error("Could not read details for waypoint "
					+ parent.getWayPoint(), e);
			return null;
		}
	}

	private static void setDirty(CacheHolderDetail impl) {
		dirtyObjects.put(impl.getParent().getWayPoint(), impl);
	}

	@Override
	public void addCacheLogs(LogList newLogs) {
		CacheHolderDetail impl = getImpl();
		impl.addCacheLogs(newLogs);
		setDirty(impl);
	}

	@Override
	public void addUserImage(ImageInfo userImage) {
		CacheHolderDetail impl = getImpl();
		impl.addUserImage(userImage);
		setDirty(impl);
	}

	@Override
	public Attributes getAttributes() {
		return getImpl().getAttributes();
	}

	@Override
	public LogList getCacheLogs() {
		return getImpl().getCacheLogs();
	}

	@Override
	public String getCacheNotes() {
		return getImpl().getCacheNotes();
	}

	@Override
	public String getCountry() {
		return getImpl().getCountry();
	}

	@Override
	public String getHints() {
		return getImpl().getHints();
	}

	@Override
	public CacheImages getImages() {
		return getImpl().getImages();
	}

	@Override
	public String getLastUpdate() {
		return getImpl().getLastUpdate();
	}

	@Override
	public CacheImages getLogImages() {
		return getImpl().getLogImages();
	}

	@Override
	public String getLongDescription() {
		return getImpl().getLongDescription();
	}

	@Override
	public Log getOwnLog() {
		return getImpl().getOwnLog();
	}

	@Override
	public String getOwnLogId() {
		return getImpl().getOwnLogId();
	}

	@Override
	public CacheHolder getParent() {
		return getImpl().getParent();
	}

	@Override
	public String getSolver() {
		return getImpl().getSolver();
	}

	@Override
	public String getState() {
		return getImpl().getState();
	}

	@Override
	public TravelbugList getTravelbugs() {
		return getImpl().getTravelbugs();
	}

	@Override
	public String getUrl() {
		return getImpl().getUrl();
	}

	@Override
	public CacheImages getUserImages() {
		return getImpl().getUserImages();
	}

	@Override
	public boolean hasImageInfo() {
		return getImpl().hasImageInfo();
	}

	@Override
	public boolean hasUnsavedChanges() {
		return getImpl().hasUnsavedChanges();
	}

	@Override
	public void setAttributes(Attributes attributes) {
		CacheHolderDetail impl = getImpl();
		impl.setAttributes(attributes);
		setDirty(impl);
	}

	@Override
	public void setCacheNotes(String notes) {
		CacheHolderDetail impl = getImpl();
		impl.setCacheNotes(notes);
		setDirty(impl);
	}

	@Override
	public void setCountry(String country) {
		CacheHolderDetail impl = getImpl();
		impl.setCountry(country);
		setDirty(impl);
	}

	@Override
	public void setHints(String hints) {
		CacheHolderDetail impl = getImpl();
		impl.setHints(hints);
		setDirty(impl);
	}

	@Override
	public void setImages(CacheImages images) {
		CacheHolderDetail impl = getImpl();
		impl.setImages(images);
		setDirty(impl);
	}

	@Override
	public void setLastUpdate(String lastUpdate) {
		CacheHolderDetail impl = getImpl();
		impl.setLastUpdate(lastUpdate);
		setDirty(impl);
	}

	@Override
	public void setLogImages(CacheImages logImages) {
		CacheHolderDetail impl = getImpl();
		impl.setLogImages(logImages);
		setDirty(impl);
	}

	@Override
	public void setLongDescription(String longDescription) {
		CacheHolderDetail impl = getImpl();
		impl.setLongDescription(longDescription);
		setDirty(impl);
	}

	@Override
	public void setOwnLog(Log ownLog) {
		CacheHolderDetail impl = getImpl();
		impl.setOwnLog(ownLog);
		setDirty(impl);
	}

	@Override
	public void setOwnLogId(String ownLogId) {
		CacheHolderDetail impl = getImpl();
		impl.setOwnLogId(ownLogId);
		setDirty(impl);
	}

	@Override
	public void setParent(CacheHolder parent) {
		CacheHolderDetail impl = getImpl();
		impl.setParent(parent);
		setDirty(impl);
	}

	@Override
	public void setSolver(String solver) {
		CacheHolderDetail impl = getImpl();
		impl.setSolver(solver);
		setDirty(impl);
	}

	@Override
	public void setState(String state) {
		CacheHolderDetail impl = getImpl();
		impl.setState(state);
		setDirty(impl);
	}

	@Override
	public void setTravelbugs(TravelbugList travelbugs) {
		CacheHolderDetail impl = getImpl();
		impl.setTravelbugs(travelbugs);
		setDirty(impl);
	}

	@Override
	public void setUnsavedChanges(boolean unsavedChanges) {
		CacheHolderDetail impl = getImpl();
		impl.setUnsavedChanges(unsavedChanges);
		setDirty(impl);
	}

	@Override
	public void setUrl(String url) {
		CacheHolderDetail impl = getImpl();
		impl.setUrl(url);
		setDirty(impl);
	}

	@Override
	public void setUserImages(CacheImages userImages) {
		CacheHolderDetail impl = getImpl();
		impl.setUserImages(userImages);
		setDirty(impl);
	}

	@Override
	public void stripLogsToMaximum(int maxSize) {
		CacheHolderDetail impl = getImpl();
		impl.stripLogsToMaximum(maxSize);
		setDirty(impl);
	}

	@Override
	public void update(ICacheHolderDetail newCh) {
		CacheHolderDetail impl = getImpl();
		impl.update(newCh);
		setDirty(impl);
	}

}
