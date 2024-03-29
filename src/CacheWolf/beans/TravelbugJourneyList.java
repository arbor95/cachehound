package CacheWolf.beans;

/**
 * A list of @see TravelbugJourney s.
 */
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import CacheWolf.Global;
import CacheWolf.util.SafeXML;
import de.cachehound.util.collections.ComparatorHelper;
import ewesoft.xml.MinML;
import ewesoft.xml.sax.AttributeList;

public class TravelbugJourneyList extends MinML {

	/** The Vector holdin the travelbug journeys */
	private List<TravelbugJourney> tbJourneyList = new ArrayList<TravelbugJourney>();

	public TravelbugJourneyList() { // Public constructor
	}

	/** Return a TravelbugJourney */
	public TravelbugJourney getTBJourney(int i) {
		return tbJourneyList.get(i);
	}

	/** Number of TravelbugJourneys in list */
	public int size() {
		return tbJourneyList.size();
	}

	/** Clear the list */
	public void clear() {
		tbJourneyList.clear();
	}

	/** Add a TravelbugJourney to the list */
	public void add(TravelbugJourney tb) {
		tbJourneyList.add(tb);
	}

	/** Remove an element of the list */
	public void remove(int i) {
		tbJourneyList.remove(i);
	}

	/** Add e Travelbug pick-up to the list (creating a new Journey) */
	public void addTbPickup(Travelbug tb, String profile, String waypoint) {
		tbJourneyList.add(new TravelbugJourney(tb, profile, waypoint));
	}

	/**
	 * Add a Travelbug drop to the list for a given Travelbug which must be in
	 * the list
	 */
	public void addTbDrop(Travelbug tb, String profile, String waypoint) {
		int i = findTB(tb);
		if (i >= 0) {
			getTBJourney(i).dropTravelbug(profile, waypoint);
		}
	}

	/** Find a travelbug in the list */
	private int findTB(Travelbug tb) {
		for (int i = size() - 1; i >= 0; i--) {
			if (tb.getName().equals(getTBJourney(i).getTb().getName()))
				return i;
		}
		return -1;
	}

	/**
	 * Count the number of journeys where at least one log still needs to be
	 * done
	 */
	public int countNonLogged() {
		int count = 0;
		for (int i = size() - 1; i >= 0; i--)
			if (!getTBJourney(i).bothLogsDone())
				count++;
		return count;
	}

	/**
	 * Return a list of the travelbugs still in my possession
	 * 
	 * @return
	 */
	public TravelbugList getMyTravelbugs() {
		TravelbugList tbl = new TravelbugList();
		int size = size();
		for (int i = 0; i < size; i++) {
			TravelbugJourney tbj = getTBJourney(i);
			if (tbj.inMyPosession())
				tbl.add(tbj.getTb());
		}
		return tbl;
	}

	// Variables needed for reading the TB list
	private String lastName;
	private StringBuilder xmlElement = new StringBuilder(200);
	private TravelbugJourney tbJ;

	/**
	 * Method to open and parse the travelbugs.xml file which contains the
	 * tavelbugs which were picked up and dropped by us.
	 */
	public boolean readTravelbugsFile() {
		try {
			java.io.File file = new java.io.File(Global.getPref().getBaseDir(),
					"travelbugs.xml");
			ewe.io.Reader r = new ewe.io.InputStreamReader(
					new ewe.io.FileInputStream(file.getAbsolutePath()));
			parse(r);
			r.close();
		} catch (Exception e) {
			if (e instanceof NullPointerException)
				Global.getPref().log(
						"Error reading travelbugs.xml: NullPointerException in Element "
								+ lastName + ". Wrong attribute?", e, true);
			else
				Global.getPref().log("Error reading travelbugs.xml: ", e);
			return false;
		}
		return true;
	}

	/**
	 * Method that gets called when a new element has been identified in
	 * travelbugs.xml
	 */
	public void startElement(String name, AttributeList atts) {
		// Vm.debug("name = "+name);
		lastName = name;
		if (name.equals("tbj")) {
			tbJ = new TravelbugJourney(atts.getValue("id"), "", atts
					.getValue("trackingNo"), SafeXML.cleanback(atts
					.getValue("fromProfile")), atts.getValue("fromWaypoint"),
					atts.getValue("fromDate"), atts.getValue("fromLogged"),
					SafeXML.cleanback(atts.getValue("toProfile")), atts
							.getValue("toWaypoint"), atts.getValue("toDate"),
					atts.getValue("toLogged"), "");
		}
	}

	public void characters(char ch[], int start, int length) {
		xmlElement.append(ch, start, length); // Collect the mission
	}

	public void endElement(String tag) {
		if (tag.equals("tbj")) {
			tbJ.getTb().setMission(xmlElement.toString());
			tbJourneyList.add(tbJ);
			xmlElement.delete(0, xmlElement.length());
		}
		if (tag.equals("name")) {
			tbJ.getTb().setName(xmlElement.toString());
			xmlElement.delete(0, xmlElement.length());
		}
	}

	/**
	 * Method to save current travelbugs in the travelbugs.xml file
	 */
	public void saveTravelbugsFile() {
		java.io.File baseDir = Global.getPref().getBaseDir();
		try {
			java.io.File backup = new java.io.File(baseDir, "travelbugs.bak");
			if (backup.exists())
				backup.delete();
			java.io.File travelbugs = new java.io.File(baseDir,
					"travelbugs.xml");
			travelbugs.renameTo(new java.io.File(baseDir, "travelbugs.bak"));
		} catch (Exception ex) {
			Global.getPref().log(
					"Error deleting backup or renaming travelbugs.xml");
		}
		File file = new File(baseDir, "travelbugs.xml");
		try {
			PrintWriter outp = new PrintWriter(new BufferedWriter(
					new FileWriter(file)));
			outp.print("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
			outp.print("<travelbugJourneys>\n");
			int size = tbJourneyList.size();
			for (int i = 0; i < size; i++)
				outp.print(tbJourneyList.get(i).toXML());
			outp.print("</travelbugJourneys>\n");
			outp.close();
		} catch (Exception e) {
			Global.getPref().log("Problem saving: " + file.getAbsolutePath(),
					e, true);
		}
	}

	/** Sort the list of travelbug journeys by any column */
	public void sort(int column, boolean ascending) {
		Comparator<TravelbugJourney> comp;

		if (ascending) {
			comp = new tbjComparer(column);
		} else {
			comp = ComparatorHelper.invert(new tbjComparer(column));
		}
		Collections.sort(tbJourneyList, comp);
	}

	/**
	 * Sort only part of the travelbug journey list. This is used to sort the
	 * non-logged journeys, which are at the start of the list
	 * 
	 * @param column
	 *            Column to sort by
	 * @see TravelbugJourney
	 * @param ascending
	 *            Sort order
	 * @param nElem
	 *            Number of elements to sort
	 */
	public void sortFirstHalf(int column, boolean ascending, int nElem) {
		Comparator<TravelbugJourney> comp;

		if (ascending) {
			comp = new tbjComparer(column);
		} else {
			comp = ComparatorHelper.invert(new tbjComparer(column));
		}
		Collections.sort(tbJourneyList.subList(0, nElem), comp);
	}

	private class tbjComparer implements Comparator<TravelbugJourney> {
		private int col;

		tbjComparer(int column) {
			col = column;
		}

		public int compare(TravelbugJourney o1, TravelbugJourney o2) {
			Object oo1 = o1.getElementByNumber(col);
			Object oo2 = o2.getElementByNumber(col);
			return oo1.toString().compareTo(oo2.toString());
		}
	}
}
