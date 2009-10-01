/**
 * Kommandozeilenversion der Fortschrittsanzeige. Zeigt den Fortschritt in
 * Zeilen wie folgender an:
 * 
 * (17/42) Blahfasel
 */

package de.cachehound.gui.cmdline;

import de.cachehound.gui.interfaces.IProgressBar;

public class ProgressBar implements IProgressBar {
	public ProgressBar(int max) {
		this.max = max;
		this.current = 0;
	}

	@Override
	public void tick(String status) {
		System.out.println(String.format("(%d/%d) %s", ++current, max, status));
	}

	private int max, current;
}
