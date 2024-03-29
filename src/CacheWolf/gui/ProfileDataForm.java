package CacheWolf.gui;

import CacheWolf.Global;
import CacheWolf.beans.CWPoint;
import CacheWolf.beans.Preferences;
import CacheWolf.beans.Profile;
import CacheWolf.util.MyLocale;
import ewe.ui.CellPanel;
import ewe.ui.ControlEvent;
import ewe.ui.Event;
import ewe.ui.Form;
import ewe.ui.FormBase;
import ewe.ui.UIConstants;
import ewe.ui.mButton;
import ewe.ui.mLabel;

/**
 * This form displays profile specific data. It allows the copying of the
 * current centre to the profile centre
 */
public class ProfileDataForm extends Form {

	private mButton btnOK, btnCurrentCentre, btnProfileCentre, btnCur2Prof,
			btnProf2Cur;
	Preferences pref;
	Profile profile;
	CellPanel content = new CellPanel();

	/**
	 */
	public ProfileDataForm(Preferences p, Profile prof) {
		super();
		pref = p;
		profile = prof;

		resizable = false;
		content.setText(MyLocale.getMsg(1115, "Centre"));
		content.borderStyle = UIConstants.BDR_RAISEDOUTER
				| UIConstants.BDR_SUNKENINNER | UIConstants.BF_RECT;
		// defaultTags.set(this.INSETS,new Insets(2,2,2,2));
		title = MyLocale.getMsg(1118, "Profile") + ": " + profile.name;
		content.addNext(new mLabel(MyLocale.getMsg(1116, "Current")));
		content.addLast(btnCurrentCentre = new mButton(pref.getCurCenter()
				.toString()), HSTRETCH, HFILL | LEFT);
		content.addNext(new mLabel("      "), HSTRETCH, HFILL);
		content.addNext(btnCur2Prof = new mButton("   v   "), DONTSTRETCH,
				DONTFILL | LEFT);
		content.addNext(new mLabel(MyLocale.getMsg(1117, "copy")));
		content.addLast(btnProf2Cur = new mButton("   ^   "), DONTSTRETCH,
				DONTFILL | RIGHT);
		content.addNext(new mLabel(MyLocale.getMsg(1118, "Profile")));
		content.addLast(btnProfileCentre = new mButton(profile.getCenter()
				.toString()), HSTRETCH, HFILL | LEFT);
		addLast(content, HSTRETCH, HFILL);
		addLast(new mLabel(""), VSTRETCH, FILL);
		// addNext(btnCancel = new
		// mButton(MyLocale.getMsg(1604,"Cancel")),DONTSTRETCH,DONTFILL|LEFT);
		addLast(btnOK = new mButton("OK"), DONTSTRETCH, HFILL | RIGHT);
	}

	/**
	 * The event handler to react to a users selection. A return value is
	 * created and passed back to the calling form while it closes itself.
	 */
	public void onEvent(Event ev) {
		if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED) {
			/*
			 * if (ev.target == btnCancel){ close(-1); }
			 */
			if (ev.target == btnOK) {
				close(1);
			}
			if (ev.target == btnCurrentCentre) {
				CoordsScreen cs = new CoordsScreen();
				cs.setFields(pref.getCurCenter(), CWPoint.CW);
				if (cs.execute() == FormBase.IDOK) {
					pref.setCurCenter(cs.getCoords());
					btnCurrentCentre.setText(pref.getCurCenter().toString());
					Global.getProfile().updateBearingDistance();
				}
			}
			if (ev.target == btnProfileCentre) {
				CoordsScreen cs = new CoordsScreen();
				cs.setFields(profile.getCenter(), CWPoint.CW);
				if (cs.execute() == FormBase.IDOK) {
					profile.notifyUnsavedChanges(cs.getCoords().equals(
							profile.getCenter()));
					profile.setCenter(cs.getCoords());
					btnProfileCentre.setText(profile.getCenter().toString());
				}
			}
			if (ev.target == btnCur2Prof) {
				profile.notifyUnsavedChanges(pref.getCurCenter().equals(
						profile.getCenter()));
				profile.setCenter(pref.getCurCenter());
				btnProfileCentre.setText(profile.getCenter().toString());
			}
			if (ev.target == btnProf2Cur) {
				pref.setCurCenter(profile.getCenter());
				btnCurrentCentre.setText(pref.getCurCenter().toString());
				Global.getProfile().updateBearingDistance();
			}
		}
		super.onEvent(ev);
	}

}
