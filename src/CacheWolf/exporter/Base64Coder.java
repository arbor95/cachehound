package CacheWolf.exporter;

public class Base64Coder {
	private static char[] map1 = new char[64];
	private static byte[] map2;

	static {
		int i = 0;
		for (char c = 'A'; c <= 'Z'; c = (char) (c + '\1')) {
			map1[(i++)] = c;
		}
		for (char c = 'a'; c <= 'z'; c = (char) (c + '\1')) {
			map1[(i++)] = c;
		}
		for (char c = '0'; c <= '9'; c = (char) (c + '\1')) {
			map1[(i++)] = c;
		}
		map1[(i++)] = '+';
		map1[(i++)] = '/';
		map2 = new byte[128];
		for (i = 0; i < map2.length; ++i) {
			map2[i] = -1;
		}
		for (i = 0; i < 64; ++i)
			map2[map1[i]] = (byte) i;
	}

	public static String encodeString(String s) {
		return new String(encode(s.getBytes()));
	}

	public static char[] encode(byte[] in) {
		return encode(in, in.length);
	}

	public static char[] encode(byte[] in, int iLen) {
		int oDataLen = (iLen * 4 + 2) / 3;
		int oLen = (iLen + 2) / 3 * 4;
		char[] out = new char[oLen];
		int ip = 0;
		for (int op = 0; ip < iLen; ++op) {
			int i0 = in[(ip++)] & 0xFF;
			int i1 = (ip >= iLen) ? 0 : in[(ip++)] & 0xFF;
			int i2 = (ip >= iLen) ? 0 : in[(ip++)] & 0xFF;
			int o0 = i0 >>> 2;
			int o1 = (i0 & 0x3) << 4 | i1 >>> 4;
			int o2 = (i1 & 0xF) << 2 | i2 >>> 6;
			int o3 = i2 & 0x3F;
			out[(op++)] = map1[o0];
			out[(op++)] = map1[o1];
			out[op] = ((op >= oDataLen) ? '=' : map1[o2]);
			++op;
			out[op] = ((op >= oDataLen) ? '=' : map1[o3]);
		}

		return out;
	}

	public static String decodeString(String s) {
		return new String(decode(s));
	}

	public static byte[] decode(String s) {
		return decode(s.toCharArray());
	}

	public static byte[] decode(char[] in) {
		int iLen = in.length;
		if (iLen % 4 != 0)
			throw new IllegalArgumentException(
					"Length of Base64 encoded input string is not a multiple of 4.");
		do
			--iLen;
		while ((iLen > 0) && (in[(iLen - 1)] == '='));
		int oLen = iLen * 3 / 4;
		byte[] out = new byte[oLen];
		int ip = 0;
		int op = 0;
		while (ip < iLen) {
			int i0 = in[(ip++)];
			int i1 = in[(ip++)];
			int i2 = (ip >= iLen) ? 'A' : in[(ip++)];
			int i3 = (ip >= iLen) ? 'A' : in[(ip++)];
			if ((i0 > 127) || (i1 > 127) || (i2 > 127) || (i3 > 127))
				throw new IllegalArgumentException(
						"Illegal character in Base64 encoded data.");
			int b0 = map2[i0];
			int b1 = map2[i1];
			int b2 = map2[i2];
			int b3 = map2[i3];
			if ((b0 < 0) || (b1 < 0) || (b2 < 0) || (b3 < 0))
				throw new IllegalArgumentException(
						"Illegal character in Base64 encoded data.");
			int o0 = b0 << 2 | b1 >>> 4;
			int o1 = (b1 & 0xF) << 4 | b2 >>> 2;
			int o2 = (b2 & 0x3) << 6 | b3;
			out[(op++)] = (byte) o0;
			if (op < oLen)
				out[(op++)] = (byte) o1;
			if (op < oLen)
				out[(op++)] = (byte) o2;
		}
		return out;
	}
}