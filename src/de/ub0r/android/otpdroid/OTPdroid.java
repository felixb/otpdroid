package de.ub0r.android.otpdroid;

import java.util.StringTokenizer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * OTPdroid's main activity.
 * 
 * @author flx
 */
public class OTPdroid extends Activity implements BeerLicense {
	/** Tag for output. */
	private static final String TAG = "OTPdroid";

	/** Dialog: about. */
	private static final int DIALOG_ABOUT = 0;
	/** Dialog: update. */
	private static final int DIALOG_UPDATE = 1;

	/** Pref: save passphrase. */
	public static final String PREF_SAVEPASSPHRASE = "savePassphrase";
	/** Pref: saved passphrase. */
	public static final String PREF_SAVEDPASSPHRASE = "savedPassphrase";
	/** Pref: save sequence. */
	public static final String PREF_SAVESEQUENCE = "saveSequence";
	/** Pref: saved hash sequence. */
	public static final String PREF_SAVEDSEQUENCE = "savedSequence";
	/** Pref: saved hash count. */
	public static final String PREF_SAVEDCOUNT = "savedCount";
	/** Pref: saved hash method. */
	public static final String PREF_SAVEDHASHMETHOD = "savedHashMethod";

	/** Phone's imei. */
	private String imei = null;
	/** Phone's simid. */
	private String simid = null;

	/** Button for calc Otp. */
	private Button calc;
	/** EditText for passphrase. */
	private EditText passphrase;
	/** EditText for challange. */
	private EditText challenge;
	/** EditText for response. */
	private EditText response;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		boolean savePassphrase;

		this.setContentView(R.layout.main);

		this.passphrase = (EditText) this.findViewById(R.id.passphrase);
		this.challenge = (EditText) this.findViewById(R.id.challenge);
		this.calc = (Button) this.findViewById(R.id.calculate);
		this.response = (EditText) this.findViewById(R.id.response);

		if (this.imei == null || this.simid == null) {
			this.loadKeys();
		}

		final SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);
		savePassphrase = settings.getBoolean(OTPdroid.PREF_SAVEPASSPHRASE,
				false);

		if (savePassphrase && this.passphrase.getText().toString().length() < 1) {
			this.loadPassphrase(settings);
		}

		this.calc.setOnClickListener(new Button.OnClickListener() {
			public void onClick(final View v) {
				OTPdroid.this.calc();
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean onCreateOptionsMenu(final Menu menu) {
		MenuInflater inflater = this.getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.item_about: // start about dialog
			this.showDialog(DIALOG_ABOUT);
			return true;
		case R.id.item_settings: // start settings activity
			this.startActivity(new Intent(this, Preferences.class));
			return true;
		case R.id.item_more:
			try {
				this.startActivity(new Intent(Intent.ACTION_VIEW, Uri
						.parse("market://search?q=pub:\"Felix Bechstein\"")));
			} catch (ActivityNotFoundException e) {
				Log.e(TAG, "no market", e);
			}
			return true;
		default:
			return false;
		}
	}

	/**
	 * Calculate the response.
	 */
	private void calc() {
		byte algo = 0x00;
		int seq = -1;
		int tokens;
		String seed = null;
		Otp otpwd;

		StringTokenizer t = new StringTokenizer(this.challenge.getText()
				.toString().toLowerCase());

		tokens = t.countTokens();

		if (tokens == 2) {
			String tmp;

			algo = Otp.MD5;
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

			if (tmp.startsWith("Otp-") || tmp.startsWith("md4")
					|| tmp.startsWith("md5") || tmp.startsWith("sha1")) {
				if (tmp.endsWith("md4")) {
					// algo=Otp.MD4;
				} else if (tmp.endsWith("sha1")) {
					algo = Otp.SHA1;
				} else if (tmp.endsWith("md5")) {
					algo = Otp.MD5;
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
			otpwd = new Otp(seq, seed, this.passphrase.getText().toString(),
					algo);
			otpwd.calc();

			this.response.setText(otpwd.toString());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected final void onStop() {
		super.onStop();

		boolean savePassphrase;

		final SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);
		final Editor editor = settings.edit();

		savePassphrase = settings.getBoolean(OTPdroid.PREF_SAVEPASSPHRASE,
				false);

		if (savePassphrase) {
			this.savePassphrase(editor);
		} else {
			editor.putString(OTPdroid.PREF_SAVEDPASSPHRASE, "");
		}

		editor.commit();
	}

	/**
	 * Load passphrase from preferences.
	 * 
	 * @param settings
	 *            preferences
	 */
	private void loadPassphrase(final SharedPreferences settings) {
		AES aes = new AES();
		int l;

		aes.genKey((this.imei + this.simid).getBytes());
		aes.setInputBase64(settings
				.getString(OTPdroid.PREF_SAVEDPASSPHRASE, ""));
		l = aes.decrypt();

		if (l > 0) {
			this.passphrase.setText(new String(aes.getOutput()));
			this.challenge.requestFocus();
		}
	}

	/**
	 * Save passphrase to preferences.
	 * 
	 * @param editor
	 *            editor
	 */
	private void savePassphrase(final Editor editor) {
		AES aes = new AES();
		int l;

		aes.genKey((this.imei + this.simid).getBytes());
		aes.setInput(this.passphrase.getText().toString().getBytes());
		l = aes.encrypt();

		if (l > -1) {
			editor.putString(OTPdroid.PREF_SAVEDPASSPHRASE, aes
					.getOutputBase64());
		}
	}

	/**
	 * Load imei and simid from phone.
	 */
	private void loadKeys() {
		TelephonyManager phone = (TelephonyManager) this
				.getApplicationContext().getSystemService(
						Context.TELEPHONY_SERVICE);

		this.imei = phone.getDeviceId();
		this.simid = phone.getSimSerialNumber();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected final Dialog onCreateDialog(final int id) {
		AlertDialog.Builder builder;
		Dialog d;
		switch (id) {
		case DIALOG_ABOUT:
			d = new Dialog(this);
			d.setContentView(R.layout.about);
			d.setTitle(this.getString(R.string.about_) + " v"
					+ this.getString(R.string.app_version));
			return d;
		case DIALOG_UPDATE:
			builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.changelog_);
			final String[] changes = this.getResources().getStringArray(
					R.array.updates);
			final StringBuilder buf = new StringBuilder(changes[0]);
			for (int i = 1; i < changes.length; i++) {
				buf.append("\n\n");
				buf.append(changes[i]);
			}
			builder.setIcon(android.R.drawable.ic_menu_info_details);
			builder.setMessage(buf.toString());
			builder.setCancelable(true);
			builder.setPositiveButton(android.R.string.ok,
					new DialogInterface.OnClickListener() {
						public void onClick(final DialogInterface dialog,
								final int id) {
							dialog.cancel();
						}
					});
			return builder.create();
		default:
			return null;
		}
	}
}
