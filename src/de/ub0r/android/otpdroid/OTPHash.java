package de.ub0r.android.otpdroid;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class OTPHash implements BeerLicense {
	public OTPHash() {
	}

	private byte[] md4(final String str) {
		return this.md4(str.getBytes());
	}

	private byte[] md4(final byte[] bytes) {
		byte[] rc = new byte[8];

		MessageDigest digest;

		try {
			int i;

			digest = MessageDigest.getInstance("MD4");
			digest.reset();
			digest.update(bytes);

			byte[] digested = digest.digest();

			for (i = 0; i < 8; i++) {
				rc[i] = (byte) ((digested[i] ^ digested[i + 8]) & 0xff);
			}
		} catch (NoSuchAlgorithmException eNoAlg) {
			rc = null;
		}

		return rc;
	}

	private byte[] md5(final String str) {
		return this.md5(str.getBytes());
	}

	private byte[] md5(final byte[] bytes) {
		byte[] rc = new byte[8];

		MessageDigest digest;

		try {
			int i;

			digest = MessageDigest.getInstance("MD5");
			digest.reset();
			digest.update(bytes);

			byte[] digested = digest.digest();

			for (i = 0; i < 8; i++) {
				rc[i] = (byte) ((digested[i] ^ digested[i + 8]) & 0xff);
			}
		} catch (NoSuchAlgorithmException eNoAlg) {
			rc = null;
		}

		return rc;
	}

	private byte[] sha1(final String str) {
		return this.sha1(str.getBytes());
	}

	private byte[] sha1(final byte[] bytes) {
		byte[] rc = new byte[8];

		MessageDigest digest;

		try {
			int i;
			int j;

			digest = MessageDigest.getInstance("SHA1");
			digest.reset();
			digest.update(bytes);

			byte[] b = digest.digest();
			int[] digested = new int[5];

			for (j = 0; j < digested.length; j++) {
				digested[j] = ((b[j * 4 + 0] & 0xff) << 24)
						| ((b[j * 4 + 1] & 0xff) << 16)
						| ((b[j * 4 + 2] & 0xff) << 8) | (b[j * 4 + 3] & 0xff);
			}

			digested[0] ^= digested[2];
			digested[1] ^= digested[3];
			digested[0] ^= digested[4];

			for (i = 0, j = 0; j < 8; i++, j += 4) {
				rc[j] = (byte) (digested[i] & 0xff);
				rc[j + 1] = (byte) ((digested[i] >> 8) & 0xff);
				rc[j + 2] = (byte) ((digested[i] >> 16) & 0xff);
				rc[j + 3] = (byte) ((digested[i] >> 24) & 0xff);
			}
		} catch (NoSuchAlgorithmException eNoAlg) {
			rc = null;
		}

		return rc;
	}

	public byte[] calcMD4(final String passphrase, final String seed, int seq) {
		byte[] rc;

		rc = this.md4(seed + passphrase);
		while (seq > 0) {
			rc = this.md4(rc);
			seq--;
		}

		return rc;
	}

	public byte[] calcMD5(final String passphrase, final String seed, int seq) {
		byte[] rc;

		rc = this.md5(seed + passphrase);
		while (seq > 0) {
			rc = this.md5(rc);
			seq--;
		}

		return rc;
	}

	public byte[] calcSHA1(final String passphrase, final String seed, int seq) {
		byte[] rc;

		rc = this.sha1(seed + passphrase);
		while (seq > 0) {
			rc = this.sha1(rc);
			seq--;
		}

		return rc;
	}
}