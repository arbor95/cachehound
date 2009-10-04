package de.cachehound.util.gpsbabel;

/**
 * Laut {@code man gpsbabel} sieht ein Aufruf von gpsbabel folgendermassen aus:
 * <pre>
 * /usr/bin/gpsbabel [options] -i INTYPE -f INFILE -o OUTTYPE -F OUTFILE
 * /usr/bin/gpsbabel [options] -i INTYPE -o OUTTYPE INFILE [OUTFILE]
 * </pre>
 * Diese Klasse kapselt ein Paar aus INTYPE, INFILE (bzw. OUTTYPE, OUTFILE). Der
 * Type kann dabei noch optionale Optionen enthalten. Implementierungen dieser
 * Klasse sollten für jeden Parameter einen eigenen Setter anbieten. {@code
 * getType} liefert alles zusammen im von gpsbabel erwarteten Format zurück.
 * 
 * @author uo
 * 
 */
public interface IBabelFormat {
	/**
	 * Liefert den Typ zusammen mit den Optionen als Komma-separierten String
	 * zurück.
	 */
	public String getType();

	/**
	 * Der Datei- oder Devicename, von dem gelesen bzw. in den geschrieben wird.
	 */
	public String getFileName();
}
