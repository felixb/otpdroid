package de.ub0r.android.otpdroid;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AES {
	private byte[] key;
	private byte[] input;
	private byte[] output;

	public AES() {
		this.key = null;
		this.input = null;
		this.output = null;
	}

	private void wipeInput() {
		if (this.input != null) {
			int l = this.input.length;

			for (int i = 0; i < l; i++) {
				this.input[i] = 0x00;
			}

			this.input = null;
		}
	}

	public byte[] getOutput() {
		this.wipeInput();
		return this.output;
	}

	public void setInput(final byte[] input) {
		this.wipeInput();
		this.input = input;
	}

	public boolean genKey(final byte[] input) {
		boolean rc = false;

		this.wipeInput();
		this.output = null;

		try {
			MessageDigest digest = MessageDigest.getInstance("SHA256");

			this.key = digest.digest(input);

			rc = true;
		} catch (NoSuchAlgorithmException eNoAlg) {
		}

		return rc;
	}

	public int decrypt() {
		int rc = -1;

		SecretKeySpec skeySpec = new SecretKeySpec(this.key, "AES");

		try {
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, skeySpec);

			this.output = cipher.doFinal(this.input);

			rc = this.output.length;
		} catch (Exception e) {
			this.output = null;
		}

		return rc;
	}

	public int encrypt() {
		int rc = -1;

		SecretKeySpec skeySpec = new SecretKeySpec(this.key, "AES");

		try {
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec);

			this.output = cipher.doFinal(this.input);
			this.wipeInput();

			rc = this.output.length;
		} catch (Exception e) {
			this.output = null;
		}

		return rc;
	}

	public String getOutputBase64() {
		return new String(Base64.encode(this.getOutput()));
	}

	public void setInputBase64(final String base64encoded) {
		this.input = Base64.decode(base64encoded.getBytes());
	}
}
