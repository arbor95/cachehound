package CacheWolf.gui;

import CacheWolf.Global;
import CacheWolf.beans.CWPoint;
import CacheWolf.beans.CacheHolder;
import CacheWolf.navi.Metrics;
import CacheWolf.util.Common;
import CacheWolf.util.MyLocale;
import de.cachehound.factory.CWPointFactory;
import de.cachehound.types.CacheType;
import ewe.fx.Dimension;
import ewe.fx.FontMetrics;
import ewe.ui.CellConstants;
import ewe.ui.CellPanel;
import ewe.ui.CheckBoxGroup;
import ewe.ui.ControlEvent;
import ewe.ui.Event;
import ewe.ui.FormBase;
import ewe.ui.ScrollBarPanel;
import ewe.ui.mButton;
import ewe.ui.mCheckBox;
import ewe.ui.mChoice;
import ewe.ui.mInput;
import ewe.ui.mLabel;
import ewe.ui.formatted.TextDisplay;

/**
 *	Class to create the panel to do calculation with waypoints<br>
 *	Also allows for creation of a custom waypoint.<br>
 *	Class ID 1400
 */

/**
 * Wrapper class to pass bearing and distance
 */
class BearingDistance {
	public double degrees;
	public double distance;

	public BearingDistance() {
		this.degrees = 0;
		this.distance = 0;
	}

	public BearingDistance(double degrees, double distance) {
		this.degrees = degrees;
		this.distance = distance;
	}
}

public class CalcPanel extends CellPanel {

	private mCheckBox chkDMM, chkDMS, chkDD, chkUTM, chkGK;
	private CheckBoxGroup chkFormat = new CheckBoxGroup();
	private mChoice chcDistUnit;
	private mInput inpBearing, inpDistance;
	private TextDisplay txtOutput;
	private mButton btnCalc, btnClear, btnSave;
	private BearingDistance bd = new BearingDistance();
	private CWPoint coordInp = CWPointFactory.getInstance().createInvalid();
	private CWPoint coordOut = CWPointFactory.getInstance().createInvalid();
	// Needed for creation of new waypoint
	private MainTab mainT;
	// different panels to avoid spanning
	private CellPanel TopP = new CellPanel();
	private CellPanel BottomP = new CellPanel();

	private String lastWaypoint = "";

	private int currFormat;
	private mButton btnChangeLatLon;

	public CalcPanel() {
		mainT = Global.mainTab;

		TopP.addNext(chkDD = new mCheckBox("d.d°"), CellConstants.DONTSTRETCH,
				CellConstants.WEST);
		TopP.addNext(chkDMM = new mCheckBox("d°m.m\'"),
				CellConstants.DONTSTRETCH, CellConstants.WEST);
		TopP.addNext(chkDMS = new mCheckBox("d°m\'s\""),
				CellConstants.DONTSTRETCH, CellConstants.WEST);
		TopP.addNext(chkUTM = new mCheckBox("UTM"), CellConstants.DONTSTRETCH,
				CellConstants.WEST);
		TopP.addLast(chkGK = new mCheckBox("GK"), CellConstants.DONTSTRETCH,
				CellConstants.WEST);

		chkDD.setGroup(chkFormat);
		chkDMM.setGroup(chkFormat);
		chkDMS.setGroup(chkFormat);
		chkUTM.setGroup(chkFormat);
		chkGK.setGroup(chkFormat);
		chkFormat.setInt(CWPoint.DMM);
		btnChangeLatLon = new mButton();
		TopP.addLast(btnChangeLatLon, CellConstants.HSTRETCH,
				(CellConstants.HFILL | CellConstants.WEST));
		// inpBearing and direction, unit for inpDistance
		BottomP.addNext(new mLabel(MyLocale.getMsg(1403, "Bearing")),
				CellConstants.DONTSTRETCH,
				(CellConstants.DONTFILL | CellConstants.WEST));
		BottomP.addLast(new mLabel(MyLocale.getMsg(1404, "Distance")),
				CellConstants.DONTSTRETCH,
				(CellConstants.DONTFILL | CellConstants.WEST));
		BottomP.addNext(inpBearing = new mInput(), CellConstants.DONTSTRETCH,
				(CellConstants.DONTFILL | CellConstants.WEST));
		inpBearing.setText("0");
		BottomP.addNext(inpDistance = new mInput(), CellConstants.DONTSTRETCH,
				(CellConstants.DONTFILL | CellConstants.WEST));
		inpDistance.setText("0");
		// Check for narrow screen and reduce width of fields to avoid
		// horizontal scroll panel
		if (MyLocale.getScreenWidth() <= 240) {
			FontMetrics fm = getFontMetrics(inpBearing.getFont());
			inpBearing.setPreferredSize(fm.getTextWidth("99999999"), fm
					.getHeight() * 4 / 3);
			inpDistance.setPreferredSize(fm.getTextWidth("99999999"), fm
					.getHeight() * 4 / 3);
		}
		BottomP.addLast(
				chcDistUnit = new mChoice(new String[] { "m", "km",
						MyLocale.getMsg(1407, "steps"),
						MyLocale.getMsg(1408, "feet"),
						MyLocale.getMsg(1409, "yards"),
						MyLocale.getMsg(1410, "miles") }, 0),
				CellConstants.DONTSTRETCH,
				(CellConstants.HFILL | CellConstants.WEST)).setTag(
				CellConstants.INSETS, new ewe.fx.Insets(0, 2, 0, 0));
		if (Global.getPref().metricSystem == Metrics.METRIC) {
			chcDistUnit.setInt(0); // Meter
		} else {
			chcDistUnit.setInt(3); // Feet
		}

		// Buttons for calc and save
		BottomP.addNext(btnCalc = new mButton("Calc"),
				CellConstants.DONTSTRETCH,
				(CellConstants.DONTFILL | CellConstants.WEST));
		BottomP.addNext(btnClear = new mButton("Clear"),
				CellConstants.DONTSTRETCH,
				(CellConstants.DONTFILL | CellConstants.WEST));
		BottomP.addLast(btnSave = new mButton(MyLocale.getMsg(311,
				"Create Waypoint")), CellConstants.DONTSTRETCH,
				(CellConstants.DONTFILL | CellConstants.WEST));

		// Output
		txtOutput = new TextDisplay(3, 1); // Need to limit size for small
		// screens
		ScrollBarPanel sbp = new MyScrollBarPanel(txtOutput);
		BottomP.addLast(sbp.setTag(CellConstants.SPAN, new Dimension(4, 1)),
				CellConstants.STRETCH,
				(CellConstants.FILL | CellConstants.WEST));

		// add Panels
		this.addLast(TopP, CellConstants.HSTRETCH, CellConstants.WEST);// .setTag(SPAN,new
		// Dimension(4,1));
		this.addLast(BottomP, CellConstants.VSTRETCH, CellConstants.VFILL
				| CellConstants.WEST); // .setTag(SPAN,new Dimension(4,1));

	}

	private void readFields() {
		coordInp = CWPointFactory.getInstance().fromString(
				btnChangeLatLon.getText());
		currFormat = chkFormat.getSelectedIndex();
		bd.degrees = Common.parseDouble(inpBearing.getText());

		double rawDistance = Common.parseDouble(inpDistance.getText());
		switch (chcDistUnit.getInt()) {
		case 0:
			// meter
			bd.distance = rawDistance / 1000.0;
			break;
		case 1:
			// kilometer
			bd.distance = rawDistance;
			break;
		case 2:
			// steps
			bd.distance = rawDistance * 0.00063;
			break;
		case 3:
			// feet
			bd.distance = rawDistance * 0.0003048;
			break;
		case 4:
			// yards
			bd.distance = rawDistance * 0.0009144;
			break;
		case 5:
			// miles
			bd.distance = rawDistance * 1.609344;
			break;
		default:
			// meter
			bd.distance = rawDistance / 1000.0;
			break;
		}
		return;
	}

	// ch must be not null
	public void setFields(CacheHolder ch) {
		if (!ch.getWayPoint().equalsIgnoreCase(lastWaypoint)) {
			lastWaypoint = ch.getWayPoint();
			if (ch.getPos().isValid()) {
				inpBearing.setText("0");
				inpDistance.setText("0");

				currFormat = CWPoint.DMM;
				coordInp = new CWPoint(ch.getPos());
				setFields(coordInp, CWPoint.DMM);
			}
		}
	}

	private void setFields(CWPoint coords, int format) {
		if (format == CWPoint.CW)
			format = CWPoint.DMM;
		btnChangeLatLon.setText(coords.toString(format));
		chkFormat.selectIndex(format);
	}

	public void onEvent(Event ev) {

		// Vm.debug(ev.toString());
		if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED) {
			if (ev.target == chkFormat) {
				readFields();
				setFields(coordInp, currFormat);
				this.repaintNow();
			}

			if (ev.target == btnCalc) {
				readFields();
				coordOut = coordInp.project(bd.degrees, bd.distance);
				txtOutput
						.appendText(coordOut.toString(currFormat) + "\n", true);
			}
			if (ev.target == btnClear) {
				txtOutput.setText("");
			}
			if (ev.target == btnSave) {
				CacheHolder ch = new CacheHolder();
				readFields();
				coordOut = coordInp.project(bd.degrees, bd.distance);
				ch.setPos(coordOut);
				ch.setType(CacheType.STAGE); // TODO unfertig
				mainT.newWaypoint(ch);
			}
			if (ev.target == btnChangeLatLon) {
				CoordsScreen cs = new CoordsScreen();
				readFields();
				cs.setFields(coordInp, currFormat);
				if (cs.execute() == FormBase.IDOK) {
					btnChangeLatLon
							.setText(cs.getCoords().toString(currFormat));
					coordInp = new CWPoint(cs.getCoords());
				}
			}

		}
		super.onEvent(ev);
	}
}
