package de.ub0r.android.otpdroid;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Base64 en|de-coder
 * 
 * @version: $Revision: 1.1 $
 */
public class Base64 implements BeerLicense {
	/**
	 * The base64 alphabet in byte values
	 */
	public static final byte[] BASE64_ALPHABET = { 'A', 'B', 'C', 'D', 'E',
			'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R',
			'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e',
			'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r',
			's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4',
			'5', '6', '7', '8', '9', '+', '/' };

	/**
	 * The base64 to base256 alphabet: values > -1 are valid base64 values
	 * values == -1 are (propably) valid whitespaces values == -2 are unexpected
	 * byte values indicating a propable invalid base64 encoded array values ==
	 * -3 are impossible and indicate a corrupted base64 stream
	 */
	public static final byte[] BASE64TO256 = {
			-2,
			-2,
			-2,
			-2,
			-2,
			-2,
			-2,
			-2,
			-2, // 0-8
			-1,
			-1,
			-1,
			-1,
			-1, // 9-13 whitespaces
			-2,
			-2,
			-2,
			-2,
			-2,
			-2, // 14-29
			-2,
			-2,
			-2,
			-2,
			-2,
			-2,
			-2,
			-2,
			-2,
			-2, // 20-29
			-2,
			-2,
			-2,
			-2,
			-2,
			-2,
			-2,
			-2,
			-2,
			-2, // 30-39
			-1, // 40 whitespace
			-2,
			-2, // 41-42
			62, // 43 +
			-2,
			-2,
			-2, // 44-46
			63,
			52,
			53,
			54,
			55,
			56,
			57,
			58,
			59,
			60,
			61, // 47-57 [/-9]
			-2,
			-2,
			-2,
			-2,
			-2,
			-2,
			-2, // 58-64
			0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14,
			15,
			16,
			17,
			18,
			19,
			20,
			21,
			22,
			23,
			24,
			25, // 65-90 [A-Z]
			-2,
			-2,
			-2,
			-2,
			-2,
			-2, // 91-96
			26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39,
			40,
			41,
			42,
			43,
			44,
			45,
			46,
			47,
			48,
			49,
			50,
			51, // 97-122 [a-z]
			-2,
			-2,
			-2,
			-2,
			-2, // 123-127
			-3,
			-3,
			-3,
			-3,
			-3, // 128
			-3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
			-3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
			-3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
			-3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
			-3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
			-3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
			-3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
			-3, -3, -3, -3 // 255
	};

	public static final byte PAD = '=';
	public static final byte CR = '\r';
	public static final byte NL = '\n';

	public static byte[] decode(final byte[] in) {
		return Base64.decode(in, 0, in.length);
	}

	public static byte[] decode(final byte[] in, final int offset,
			final int length) {
		byte[] buf = new byte[4];
		int l;

		ByteArrayInputStream is = new ByteArrayInputStream(in, offset, length);
		ByteArrayOutputStream os = new ByteArrayOutputStream((int) Math
				.ceil(((length - offset) / 4.0) * 3));

		try {
			while ((l = is.read(buf)) > -1) {
				byte[] tmp = Base64.four2three(buf, 0, l);

				if (tmp != null) {
					os.write(tmp);
				}
			}
		} catch (IOException eIO) {
		}

		if (os.size() > 0) {
			return os.toByteArray();
		} else {
			return null;
		}
	}

	public static byte[] encode(final byte[] in) {
		return Base64.encode(in, 0, in.length);
	}

	public static byte[] encode(final byte[] in, final int offset,
			final int length) {
		byte[] buf = new byte[3];
		int l;

		ByteArrayInputStream is = new ByteArrayInputStream(in, offset, length);
		ByteArrayOutputStream os = new ByteArrayOutputStream((int) Math
				.ceil(((length - offset) / 3.0) * 4));

		try {
			while ((l = is.read(buf)) > -1) {
				os.write(Base64.three2four(buf, 0, l));
			}
		} catch (IOException eIO) {
		}

		return os.toByteArray();
	}

	/**
	 * Check if the byte is a valid base64 byte
	 * 
	 * @param b
	 * @return
	 */
	public static boolean isValidBase64Byte(final int b) {
		return (Base64.BASE64TO256[b] > -1 || b == Base64.PAD);
	}

	/**
	 * Convert an array of 1, 2 or 3 8bit bytes to a byte array of 4 6bit bytes
	 * 
	 * @param input
	 * @param bytes
	 * @return
	 */
	public static byte[] three2four(final byte[] input) {
		return Base64.three2four(input, 0, input.length);
	}

	public static byte[] three2four(final byte[] input, final int offset,
			final int length) {
		byte[] rc = { Base64.PAD, Base64.PAD, Base64.PAD, Base64.PAD };

		int i = 0;
		switch (length - offset) {
		case 3:
			i |= (input[offset + 2] << 24) >>> 24;
		case 2:
			i |= (input[offset + 1] << 24) >>> 16;
		case 1:
			i |= (input[offset] << 24) >>> 8;

			switch (length - offset) {
			case 3:
				rc[3] = Base64.BASE64_ALPHABET[(i) & 0x3f];
			case 2:
				rc[2] = Base64.BASE64_ALPHABET[(i >>> 6) & 0x3f];
			case 1:
				rc[1] = Base64.BASE64_ALPHABET[(i >>> 12) & 0x3f];
			case 0:
				rc[0] = Base64.BASE64_ALPHABET[(i >>> 18)];
			}
			break;
		default:
			rc = null;
		}

		return rc;
	}

	public static byte[] four2three(final byte[] input) {
		return Base64.four2three(input, 0, input.length);
	}

	public static byte[] four2three(final byte[] input, final int offset,
			final int length) {
		byte[] rc;
		int i = 0;

		if (length - offset > 4 || length - offset < 2
				|| input[offset + 1] == Base64.PAD
				|| input[offset] == Base64.PAD) {
			rc = null;
		} else {
			if (input[offset + 2] == Base64.PAD) {
				rc = new byte[1];

				i |= (BASE64TO256[input[offset + 1]] << 24) >>> 12;
				i |= (BASE64TO256[input[offset]] << 24) >>> 6;
			} else if (input[3] == Base64.PAD) {
				rc = new byte[2];

				i |= (BASE64TO256[input[offset + 2]] << 24) >>> 18;
				i |= (BASE64TO256[input[offset + 1]] << 24) >>> 12;
				i |= (BASE64TO256[input[offset]] << 24) >>> 6;
			} else {
				rc = new byte[3];

				i |= (BASE64TO256[input[offset + 3]] << 24) >>> 24;
				i |= (BASE64TO256[input[offset + 2]] << 24) >>> 18;
				i |= (BASE64TO256[input[offset + 1]] << 24) >>> 12;
				i |= (BASE64TO256[input[offset]] << 24) >>> 6;
			}

			for (int j = 0; j < rc.length; j++) {
				rc[j] = (byte) (i >> (16 - j * 8));
			}
		}

		return rc;
	}
}

/*
 * $Log: Base64.java,v $ Revision 1.1 2005/09/14 01:18:35 muaddib Base64
 * converter (with unittest)
 */