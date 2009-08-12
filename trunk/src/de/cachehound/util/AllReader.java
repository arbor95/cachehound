package de.cachehound.util;

import java.io.IOException;
import java.io.Reader;

public class AllReader extends Reader {
	private Reader r;
	private int bufSize;

	public AllReader(Reader r) {
		this.r = r;
		this.bufSize = 1024;
	}

	public AllReader(Reader r, int bufSize) {
		this.r = r;
		this.bufSize = bufSize;
	}

	@Override
	public void close() throws IOException {
		r.close();
	}

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		return r.read(cbuf, off, len);
	}

	public String readAll() throws IOException {
		char[] buffer = new char[bufSize];
		StringBuilder sb = new StringBuilder();
		int bytesRead;

		while ((bytesRead = r.read(buffer)) != -1) {
			sb.append(buffer, 0, bytesRead);
		}

		return sb.toString();
	}
}
