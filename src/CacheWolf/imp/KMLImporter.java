package CacheWolf.imp;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import CacheWolf.beans.CWPoint;
import de.cachehound.factory.CWPointFactory;
import de.cachehound.util.ewecompat.EweReader;
import ewe.sys.Convert;
import ewe.sys.Vm;
import ewesoft.xml.MinML;
import ewesoft.xml.sax.AttributeList;

/**
 * Class to import coordinates from a KML file generated from google earth. it
 * looks for <placemark><MultiGeometry><LineString><coordinates> and gathers all
 * coordinated in a vector
 * 
 * @author Bilbowolf
 * 
 */
public class KMLImporter extends MinML {

	public List<CWPoint> points = new ArrayList<CWPoint>();
	private File file;
	private String strData;
	private int status = 0;
	private static int MultiGeometry = 1;
	private static int LineString = 2;
	private static int coordinates = 3;

	public KMLImporter(File file) {
		this.file = file;
	}

	public void importFile() {
		try {
			java.io.Reader r;
			Vm.showWait(true);
			r = new FileReader(file);
			parse(new EweReader(r));
			r.close();
			Vm.showWait(false);
		} catch (Exception e) {
			// Vm.debug(e.toString());
			Vm.showWait(false);
		}
	}

	public List<CWPoint> getPoints() {
		return points;
	}

	public void startElement(String name, AttributeList atts) {
		strData = "";
		if (name.equals("MultiGeometry"))
			status = MultiGeometry;
		if (name.equals("LineString") && status == MultiGeometry)
			status = LineString;
		if (name.equals("coordinates") && status == LineString)
			status = coordinates;
	}

	public void endElement(String name) {
		if (name.equals("coordinates") && status == coordinates) {
			parseCoordinatesLine();
			// 10.09052,49.78188000000001,0
		}
		if (name.equals("LineString") && status == coordinates)
			status = LineString;
		if (name.equals("MultiGeometry") && status == LineString)
			status = 0;
	}

	public void characters(char[] ch, int start, int length) {
		String chars = new String(ch, start, length);
		strData += chars;
	}

	private void parseCoordinatesLine() {
		StringTokenizer exBlock = new StringTokenizer(strData, " ");
		StringTokenizer numbers;

		while (exBlock.hasMoreTokens()) {
			String test = exBlock.nextToken();
			// Vm.debug("==> " + test + " <==");
			numbers = new StringTokenizer(test, ",");
			// Vm.debug(numbers.nextToken());
			// Vm.debug(numbers.nextToken());
			String lon = numbers.nextToken();
			String lat = numbers.nextToken();
			points.add(CWPointFactory.getInstance().fromD(
					Double.parseDouble(lat), Double.parseDouble(lon)));
		}
	}
}