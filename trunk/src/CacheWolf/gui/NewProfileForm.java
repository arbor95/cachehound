package CacheWolf.gui;

import java.io.File;

import CacheWolf.Global;
import CacheWolf.beans.Filter;
import CacheWolf.util.MyLocale;
import ewe.ui.ControlEvent;
import ewe.ui.Event;
import ewe.ui.Form;
import ewe.ui.IKeys;
import ewe.ui.MessageBox;
import ewe.ui.TextMessage;
import ewe.ui.mButton;
import ewe.ui.mInput;

public class NewProfileForm extends Form {
	private mButton btnCancel, btnOK;
	private mInput inpDir;
	private TextMessage description;
	private File profileDir;
	
	
	private File baseDir;

	// private Profile profile;

	public NewProfileForm(File baseDir) {
		super();
		// profile=prof;
		title = MyLocale.getMsg(1111, "Create new profile:");
		addLast(inpDir = new mInput(MyLocale.getMsg(1112, "New profile name")),
				HSTRETCH, HFILL | LEFT);
		description = new TextMessage(
				MyLocale
						.getMsg(1123,
								"Click 'Next' to define the center coordinates for this profile."));
		description.setPreferredSize(240, -1);
		addLast(description, HSTRETCH, HFILL | LEFT);
		btnCancel = new mButton(MyLocale.getMsg(708, "Cancel"));
		btnCancel.setHotKey(0, IKeys.ESCAPE);
		addNext(btnCancel, HSTRETCH, LEFT);
		btnOK = new mButton(MyLocale.getMsg(1124, "Next"));
		btnOK.setHotKey(0, IKeys.ENTER);
		addLast(btnOK, HSTRETCH, HFILL | RIGHT);
		this.setPreferredSize(240, -1);
		this.baseDir = baseDir;
	}

	public void onEvent(Event ev) {
		if (ev instanceof ControlEvent && ev.type == ControlEvent.PRESSED) {
			if (ev.target == btnCancel) {
				this.close(-1);
			}
			if (ev.target == btnOK) {
				String inputText = inpDir.getDisplayText();
				profileDir = new File(baseDir, inputText);
				if (inputText.equalsIgnoreCase("maps")) {
					MessageBox mb = new MessageBox(MyLocale
							.getMsg(321, "Error"), MyLocale.getMsg(1122,
							"'maps' is reserved for the maps directory."), MBOK);
					mb.execute();
					profileDir = null;
				} else {
					if (profileDir.exists()) {
						MessageBox mb = new MessageBox(MyLocale.getMsg(321,
								"Error"), MyLocale.getMsg(1114,
								"Directory exists already."), MBOK);
						mb.execute();
						profileDir = null;
					} else {
						if (!profileDir.mkdir()) {
							MessageBox mb = new MessageBox(MyLocale.getMsg(321,
									"Error"), MyLocale.getMsg(1113,
									"Cannot create directory"), MBOK);
							mb.execute();
							profileDir = null;
							this.close(-1);
						}
						Global.getProfile().setFilterActive(
								Filter.FILTER_INACTIVE);
						this.close(0);
					}
				}
			}
		}
		super.onEvent(ev);
	}
	
	public File getProfileDir() {
		return profileDir;
	}

}
