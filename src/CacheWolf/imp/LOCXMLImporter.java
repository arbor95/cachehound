/*
CacheWolf is a software for PocketPC, Win and Linux that 
enables paperless caching. 
It supports the sites geocaching.com and opencaching.de

Copyright (C) 2006  CacheWolf development team
See http://developer.berlios.de/projects/cachewolf/
for more information.
Contact: 	bilbowolf@users.berlios.de
			kalli@users.berlios.de

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package CacheWolf.imp;

// TODO: move to CacheWOlf.imp

import java.io.File;
import java.io.FileReader;
import java.io.Reader;

import CacheWolf.beans.CacheDB;
import CacheWolf.beans.CacheHolder;
import CacheWolf.beans.Preferences;
import CacheWolf.beans.Profile;
import de.cachehound.factory.CWPointFactory;
import de.cachehound.types.CacheSize;
import de.cachehound.types.CacheType;
import de.cachehound.types.Difficulty;
import de.cachehound.types.Terrain;
import de.cachehound.util.ewecompat.EweReader;
import ewe.sys.Vm;
import ewesoft.xml.MinML;
import ewesoft.xml.sax.AttributeList;

/**
 * @author Kalle
 * 
 */
public class LOCXMLImporter extends MinML {
	boolean debugXML = false;
	CacheDB cacheDB;
	Preferences pref;
	Profile profile;
	File file;
	CacheHolder holder;

	String strData = new String();

	public LOCXMLImporter(Preferences pf, Profile prof, File f) {
		pref = pf;
		profile = prof;
		cacheDB = profile.cacheDB;
		file = f;
	}

	public void doIt() {
		try {
			Reader r;
			Vm.showWait(true);
			// Test for zip.file
			r = new FileReader(file);
			parse(new EweReader(r));
			r.close();
			// save Index
			profile.saveIndex(pref, Profile.NO_SHOW_PROGRESS_BAR);
			Vm.showWait(false);
		} catch (Exception e) {
			// Vm.debug(e.toString());
			Vm.showWait(false);
		}
	}

	public void startElement(String name, AttributeList atts) {
		if (debugXML) {
			for (int i = 0; i < atts.getLength(); i++) {
				Vm.debug(" Name: " + atts.getName(i) + " Value: "
						+ atts.getValue(i));
			}
		}
		strData = "";
		if (name.equals("name")) {
			holder = getHolder(atts.getValue("id"));
			return;
		}
		if (name.equals("coord")) {
			holder.setPos(CWPointFactory.getInstance().fromD(
					Double.parseDouble(atts.getValue("lat")),
					Double.parseDouble(atts.getValue("lon"))));
			return;
		}
	}

	public void endElement(String name) {
		if (name.equals("name")) {
			holder.setCacheName(strData);
		}

		if (name.equals("waypoint")) {
			int index;
			index = cacheDB.getIndex(holder.getWayPoint());
			if (index == -1) {
				holder.setNew(true);
				cacheDB.add(holder);
			}
			// update (overwrite) data
			else {
				holder.setNew(false);
			}
			// save all (after each cache???)
			holder.save();
			profile.saveIndex(pref, Profile.NO_SHOW_PROGRESS_BAR);
			return;
		}

		if (name.equals("link")) {
			holder.getFreshDetails().setUrl(strData);
			return;
		}
	}

	public void characters(char[] ch, int start, int length) {
		String chars = new String(ch, start, length);
		strData += chars;
		if (debugXML)
			Vm.debug(strData);
	}

	private CacheHolder getHolder(String wpt) {// See also OCXMLImporter
		CacheHolder ch;

		ch = cacheDB.get(wpt);
		if (ch == null) {
			ch = new CacheHolder();
			// TODO: once the cleanup is done, this should be move to
			// CacheHolder constructor
			ch.setType(CacheType.CUSTOM);
			ch.setTerrain(Terrain.TERRAIN_UNSET);
			ch.setDifficulty(Difficulty.DIFFICULTY_UNSET);
			ch.setCacheSize(CacheSize.NOT_CHOSEN);
			ch.setWayPoint(wpt);
		}
		return ch;
	}

}
