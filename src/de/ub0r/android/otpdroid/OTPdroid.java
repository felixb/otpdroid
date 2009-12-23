package de.ub0r.android.otpdroid;

import java.util.StringTokenizer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class OTPdroid extends Activity implements BeerLicense {
	public static final String VERSION = "1.0.0";
	public static final String PREFS = "OpiekeyPreferences";
	public static final String PREF_SAVE = "savePassphrase";
	public static final String PREF_SAVEDPASSPHRASE = "savedPassphrase";

	private String IMEI = null;
	private String SIMID = null;

	private Button calc;
	private EditText passphrase;
	private EditText challenge;
	private EditText response;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		boolean savePassphrase;

		this.setContentView(R.layout.main);

		this.passphrase = (EditText) this.findViewById(R.id.passphrase);
		this.passphrase.setHint("your passphrase");

		this.challenge = (EditText) this.findViewById(R.id.challenge);
		this.challenge.setHint("(otp-[md5|sha1] )123 abcdefgh");

		this.calc = (Button) this.findViewById(R.id.calculate);
		this.response = (EditText) this.findViewById(R.id.response);

		if (this.IMEI == null || this.SIMID == null) {
			this.loadKeys();
		}

		SharedPreferences settings = this
				.getSharedPreferences(OTPdroid.PREFS, 0);
		savePassphrase = settings.getBoolean(OTPdroid.PREF_SAVE, false);

		if (savePassphrase && this.passphrase.getText().toString().length() < 1) {
			this.loadPassphrase(settings);
		}

		this.calc.setOnClickListener(new Button.OnClickListener() {
			public void onClick(final View v) {
				OTPdroid.this.calc();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add("About");
		menu.add("Preferences");
		menu.add("Help");

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		boolean rc;
		String title;

		title = item.getTitle().toString();

		if (title.equals("About")) {
			About about = new About(this);
			about.show();

			rc = true;
		} else if (title.equals("Preferences")) {
			this.startActivity(new Intent(this, Preferences.class));

			rc = true;
		} else if (title.equals("Help")) {
			Intent i = new Intent(Intent.ACTION_VIEW);
			Uri u = Uri
					.parse("http://android.f00d.nl/opiekey/index.php?version="
							+ OTPdroid.VERSION);
			i.setData(u);
			this.startActivity(i);

			rc = true;
		} else {
			rc = false;
		}

		return rc;
	}

	public void calc() {
		byte algo = 0x00;
		int seq = -1;
		int tokens;
		String seed = null;
		otp otpwd;

		StringTokenizer t = new StringTokenizer(this.challenge.getText()
				.toString().toLowerCase());

		tokens = t.countTokens();

		if (tokens == 2) {
			String tmp;

			algo = otp.MD5;
			tmp = t.nextToken();

			try {
				seq = Integer.parseInt(tmp, 10);

				seed = t.nextToken();
			} catch (NumberFormatException eNumber) {
				this.response.setText("first argument must be an integer");
			}
		} else if (tokens == 3 || tokens == 4) {
			String tmp;

			tmp = t.nextToken();

			if (tmp.startsWith("otp-") || tmp.startsWith("md4")
					|| tmp.startsWith("md5") || tmp.startsWith("sha1")) {
				if (tmp.endsWith("md4")) {
					// algo=otp.MD4;
				} else if (tmp.endsWith("sha1")) {
					algo = otp.SHA1;
				} else if (tmp.endsWith("md5")) {
					algo = otp.MD5;
				}

				tmp = t.nextToken();

				try {
					seq = Integer.parseInt(tmp, 10);

					seed = t.nextToken();
				} catch (NumberFormatException eNumber) {
					this.response.setText("first argument must be an integer");
				}
			}
		}

		if (tokens < 2 || tokens > 4 || seq < 0 || seed == null || algo == 0x00) {
			this.response
					.setText("challenge: (opt-[md5|sha1]) 123 foobar (ext)");
		} else {
			otpwd = new otp(seq, seed, this.passphrase.getText().toString(),
					algo);
			otpwd.calc();

			this.response.setText(otpwd.toString());
		}
	}

	@Override
	protected void onStop() {
		super.onStop();

		boolean savePassphrase;

		SharedPreferences settings = this
				.getSharedPreferences(OTPdroid.PREFS, 0);
		Editor editor = settings.edit();

		savePassphrase = settings.getBoolean(OTPdroid.PREF_SAVE, false);

		if (savePassphrase) {
			this.savePassphrase(editor);
		} else {
			editor.putString(OTPdroid.PREF_SAVEDPASSPHRASE, "");
		}

		editor.commit();
	}

	private void loadPassphrase(final SharedPreferences settings) {
		AES aes = new AES();
		int l;

		aes.genKey((this.IMEI + this.SIMID).getBytes());
		aes
				.setInputBase64(settings.getString(
						OTPdroid.PREF_SAVEDPASSPHRASE, ""));
		l = aes.decrypt();

		if (l > 0) {
			this.passphrase.setText(new String(aes.getOutput()));
			this.challenge.requestFocus();
		}
	}

	private void savePassphrase(final Editor editor) {
		AES aes = new AES();
		int l;

		aes.genKey((this.IMEI + this.SIMID).getBytes());
		aes.setInput(this.passphrase.getText().toString().getBytes());
		l = aes.encrypt();

		if (l > -1) {
			editor.putString(OTPdroid.PREF_SAVEDPASSPHRASE, aes
					.getOutputBase64());
		}
	}

	private void loadKeys() {
		TelephonyManager phone = (TelephonyManager) this
				.getApplicationContext().getSystemService(
						Context.TELEPHONY_SERVICE);

		this.IMEI = phone.getDeviceId();
		this.SIMID = phone.getSimSerialNumber();
	}
}
