package CacheWolf.util;

/**
 * This is a powerfull class that is used very often. It is quicker than XML
 * parsing and should be used whenever possible to find and extract parts of a
 * string in a string.
 */
public class Extractor {
	private int startOffset; // No initialisation needed, done in constructor
	private String searchText;
	private String start;
	private String end;
	private boolean betweenonly;
	
	public static boolean INCLUDESTARTEND = false;
	public static boolean EXCLUDESTARTEND = true;

	/**
	 * Create an extractor.
	 * 
	 * @param searchxt
	 *            The string to search through.
	 * @param start
	 *            The string that denotes the start of the string to extract
	 * @param end
	 *            The string that denotes the end of the string to extract
	 * @param stringOffset
	 *            = The beginning offset from which to start the search in
	 *            startText
	 * @param only
	 *            if false the string returned will inlcude start and end; if
	 *            true it will not include start and end.
	 */
	public Extractor(String searchText, String start, String end,
			int startOffset, boolean only) {
		this.startOffset = startOffset;
		this.searchText = searchText;
		// Vm.debug("Start " + st);
		this.end = end;
		// Vm.debug("End: " + e);
		this.start = start;
		betweenonly = only;
	}

	/**
	 * Method to set the source text to be searched through
	 */
	public void setSource(String searchText) {
		this.searchText = searchText;
		// Vm.debug("Searchtext: " + searchText);
		startOffset = 0;
	}

	/**
	 * Method that informs if the search has encountered the end of the string
	 * that is being searched through.
	 */
	public boolean endOfSearch() {
		if (searchText == null || startOffset >= searchText.length())
			return true;
		else
			return false;
	}

	/**
	 * Method to find the next occurance of a string that is enclosed by that
	 * start and end string. if end is not found the string is returned til it's
	 * end.
	 */
	public String findNext() {
		String result;
		if (searchText == null) {
			return ""; // maybe null should
		}
		int idxStart = searchText.indexOf(start, startOffset);
		int idxEnd = searchText.indexOf(end, idxStart + start.length());
		// //Vm.debug("Start: " + Convert.toString(idxStart) + " End: " +
		// Convert.toString(idxEnd));
		if (idxEnd == -1)
			idxEnd = searchText.length(); // index counts from zero length
		// from 1 but the last char is not
		// included in substr and substr
		// accepts length +1 (but not
		// length+2)
		startOffset = idxEnd;
		if (idxStart > -1) {
			if (betweenonly == false) {
				if (idxEnd + end.length() >= searchText.length())
					result = searchText.substring(idxStart);
				else
					result = searchText.substring(idxStart, idxEnd + end.length());
			} else {
				result = searchText.substring(idxStart + start.length(), idxEnd);
			}
		} else {
			startOffset = searchText.length();
			result = "";
		}
		return result;
	}
}
