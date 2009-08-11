package de.cachehound.util;

import java.io.IOException;
import java.io.Reader;

/**
 * Wrapperclass for using Ewe class which expect a ewe.io.Reader.
 * 
 * @author tweety
 *
 */

@Deprecated
public class EweReader extends ewe.io.Reader{

	Reader reader;
	
	public EweReader(Reader reader) {
		this.reader = reader;
	}
	
	@Override
	public void close() throws ewe.io.IOException {
		try {
			reader.close();
		} catch (IOException e) {
			throw new ewe.io.IOException(e);
		}
	}

	@Override
	public int read(char[] cbuf, int off, int len) throws ewe.io.IOException {
		try {
			return reader.read(cbuf, off, len);
		} catch (IOException e) {
			throw new ewe.io.IOException(e);
		}
	}

}
