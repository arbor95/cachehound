/**
 * Dieses Interface ermöglicht es längerlaufenden Aktionen (z.B. Im- und
 * Exportern) Rückmeldung über den Fortschritt zu geben, ohne dass diese direkt
 * GUI-Code aufrufen müssen. Dies ist nötig, damit irgendwann mal andere
 * Interfaces (z.B. eine reine Kommandozeilen-Version) implementiert werden
 * können.
 * 
 * Jeder Fortschrittsanzeiger hat einen Maximal- und einen aktuellen Wert.
 * Der erste sollte im Konstruktor gesetzt werden. Letzterer wird bei
 * jedem Aufruf von tick() um eins erhöht.
 * 
 * Zusätzlich lässt sich noch bei jedem Aufruf von tick(String) ein Statustext
 * angeben.
 */

package de.cachehound.gui.interfaces;

public interface IProgressBar {
	/**
	 * Erhöht den aktuellen Wert des Fortschrittsanzeigers um 1 und setzt den
	 * Statustext.
	 * @param status	Der neue Statustext
	 */
	void tick(String status);

	// Eventuell brauchen wir sowas noch
	// IProgressBar createSubProgressBar(int max);
	// IProgressBar createSubProgressBar(int min, int max);
}
