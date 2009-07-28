package de.cachehound.util;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.Reader;

import org.junit.Test;


public class AllReaderTest {
	private class FakeReader extends Reader {
		private char[][] chunks;
		private int chunksread = 0;
		
		public FakeReader(char[]... chunks) {
			this.chunks = chunks;
		}
		
		@Override
		public void close() throws IOException {
			// not used by AllReader
		}

		@Override
		public int read(char[] cbuf, int off, int len) throws IOException {
			// not used by AllReader
			return 0;
		}
		
		@Override
		public int read(char[] cbuf) throws IOException {
			if (chunksread < chunks.length) {
				System.arraycopy(chunks[chunksread], 0, cbuf, 0, chunks[chunksread].length);
				return chunks[chunksread++].length;
			} else {
				return -1;
			}
		}
	}
	
	@Test public void testReadAll() throws IOException {
		Reader fr = new FakeReader(new char[]{'a', 'b'}, new char[] {'c', 'd'} );
		AllReader ar = new AllReader(fr);
		
		assertEquals("abcd", ar.readAll());
	}
}
