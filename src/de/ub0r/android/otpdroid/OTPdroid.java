package de.ub0r.android.otpdroid;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.telephony.TelephonyManager;
import android.text.ClipboardManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import de.ub0r.android.lib.Market;

/**
 * OTPdroid's main {@link FragmentActivity}.
 * 
 * @author flx
 */
public class OTPdroid extends FragmentActivity implements BeerLicense {
	/** Tag for output. */
	// private static final String TAG = "OTPdroid";

	/** Dialog: about. */
	private static final int DIALOG_ABOUT = 0;
	/** Dialog: update. */
	private static final int DIALOG_UPDATE = 1;

	/** Pref: save passphrase. */
	public static final String PREF_SAVEPASSPHRASE = "savePassphrase";
	/** Pref: encrypt passphrase with boot time. */
	public static final String PREF_ENCRYPTPASSPHRASEBYBOOTTIME = // .
	"encryptByBoottime";
	/** Pref: saved passphrase. */
	public static final String PREF_SAVEDPASSPHRASE = "passphrase";
	/** Pref: save challenge. */
	public static final String PREF_SAVECHALLENGE = "saveChallenge";
	/** Pref: saved challenge. */
	public static final String PREF_SAVEDCHALENGE = "challenge";
	/** Pref: saved sequence. */
	public static final String PREF_SAVEDSEQUENCE = "sequence";
	/** Pref: saved hash method. */
	public static final String PREF_SAVEDHASHMETHOD = "hashMethod";
	/** Pref: auto decrement sequence. */
	public static final String PREF_AUTODECREMENT = "autoDecrement";
	/** Pref: number of responses to print. */
	public static final String PREF_NUMBEROFRESPONSES = "numberOfResponses";
	/** Pref: show time. */
	public static final String PREF_SHOWTIME = "showTime";
	/** Pref: copy response. */
	public static final String PREF_COPYRESPONSE = "copyResponse";
	/** Preference's name: last version run. */
	private static final String PREFS_LAST_RUN = "lastrun";

	/** Phone's imei. */
	private String imei = null;
	/** Phone's simid. */
	private String simid = null;

	/** EditText for passphrase. */
	private EditText passphrase;
	/** Spinner for hash method. */
	private Spinner hashMethod;
	/** EditText for count. */
	private EditText sequence;
	/** EditText for challenge. */
	private EditText challenge;
	/** EditText for response. */
	private EditText response;

	/** {@link MenuItem} showing calculation button. */
	private MenuItem miCalc = null;

	/** Did user pressed the calc button? */
	private static boolean didCalc;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.main);

		this.passphrase = (EditText) this.findViewById(R.id.passphrase);
		this.hashMethod = (Spinner) this.findViewById(R.id.hash_method);
		this.sequence = (EditText) this.findViewById(R.id.sequence);
		this.challenge = (EditText) this.findViewById(R.id.challenge);
		this.response = (EditText) this.findViewById(R.id.response);

		final ArrayAdapter<CharSequence> adapter = ArrayAdapter
				.createFromResource(this, R.array.hash_methods,
						android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(// .
		android.R.layout.simple_spinner_dropdown_item);
		this.hashMethod.setAdapter(adapter);

		if (this.imei == null || this.simid == null) {
			this.loadKeys();
		}

		final SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);
		final String v0 = settings.getString(PREFS_LAST_RUN, "");
		final String v1 = this.getString(R.string.app_version);
		if (!v0.equals(v1)) {
			final SharedPreferences.Editor editor = settings.edit();
			editor.putString(PREFS_LAST_RUN, v1);
			editor.commit();
			this.showDialog(DIALOG_UPDATE);
		}
		this.loadPassphrase(settings);

		didCalc = false;
		this.decrementSequence();
	}

	/**
	 * Switch to previous sequence.
	 */
	private void prev() {
		this.response.setText("");
		try {
			final int seq = Integer
					.parseInt(this.sequence.getText().toString());
			this.sequence.setText(String.valueOf(seq - 1));
		} catch (Exception e) {
			this.response.setText(R.string.error_input);
		}
	}

	/**
	 * Switch to next sequence.
	 */
	private void next() {
		this.response.setText("");
		try {
			final int seq = Integer
					.parseInt(this.sequence.getText().toString());
			this.sequence.setText(String.valueOf(seq + 1));
		} catch (Exception e) {
			this.response.setText(R.string.error_input);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected final void onResume() {
		super.onResume();
		if (OTPdroid.didCalc) {
			this.decrementSequence();
		}
	}

	/**
	 * Decrement sequence.
	 */
	private void decrementSequence() {
		this.response.setText("");
		final SharedPreferences p = PreferenceManager
				.getDefaultSharedPreferences(this);
		if (p.getBoolean(PREF_AUTODECREMENT, false)) {
			final String s = this.sequence.getText().toString();
			if (s.length() > 0) {
				final int i = Integer.parseInt(s);
				OTPdroid.this.sequence.setText("" + (i - 1));
			}
		}
		OTPdroid.didCalc = false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean onCreateOptionsMenu(final Menu menu) {
		this.getMenuInflater().inflate(R.menu.menu, menu);
		this.miCalc = menu.findItem(R.id.item_calculate);
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.item_calculate:
			OTPdroid.this.calc();
			OTPdroid.didCalc = true;
			return true;
		case R.id.item_prev:
			this.prev();
			return true;
		case R.id.item_next:
			this.next();
			return true;
		case R.id.item_about: // start about dialog
			this.showDialog(DIALOG_ABOUT);
			return true;
		case R.id.item_settings: // start settings activity
			this.startActivity(new Intent(this, Preferences.class));
			return true;
		case R.id.item_more:
			Market.searchApp(this, "Felix+Bechstein",
					"http://code.google.com/u" + "/felix.bechstein/");
			return true;
		default:
			return false;
		}
	}

	/**
	 * Calculate the response.
	 */
	@SuppressWarnings("deprecation")
	private void calc() {
		final SharedPreferences p = PreferenceManager
				.getDefaultSharedPreferences(this);
		int n;
		try {
			n = Integer.parseInt(p.getString(PREF_NUMBEROFRESPONSES, "1"));
		} catch (NumberFormatException e) {
			n = 1;
		}
		final int numberOfResponses = n;
		final boolean showTime = p.getBoolean(PREF_SHOWTIME, true);
		final boolean copyResponse = p.getBoolean(PREF_COPYRESPONSE, true);
		final String eol = System.getProperty("line.separator");
		final ClipboardManager cbmgr = (ClipboardManager) this
				.getSystemService(CLIPBOARD_SERVICE);

		byte algo = 0x00;

		switch ((int) this.hashMethod.getSelectedItemId()) {
		case 0:
			algo = Otp.SHA1;
			break;
		case 1:
			algo = Otp.MD5;
			break;
		case 2:
			algo = Otp.MD4;
			break;
		default:
			algo = Otp.SHA1;
			break;
		}
		final byte falgo = algo;
		new AsyncTask<Void, Void, String>() {
			private Dialog d = null;

			@Override
			protected String doInBackground(final Void... arg0) {
				try {
					final long startTime = System.currentTimeMillis();
					int seq = Integer.parseInt(OTPdroid.this.sequence.getText()
							.toString());
					String finalResponse = "";
					for (int i = 0; i < numberOfResponses && seq >= 0; i++) {
						final String seed = OTPdroid.this.challenge.getText()
								.toString().toLowerCase();
						final String pass = OTPdroid.this.passphrase.getText()
								.toString();

						final Otp otpwd = new Otp(seq, seed, pass, falgo);
						otpwd.calc();
						if (copyResponse && i == 0) {
							cbmgr.setText(otpwd.toString());
						}

						if (numberOfResponses == 1) {
							finalResponse += otpwd.toString() + eol;
						} else {
							finalResponse += seq + ": " + otpwd.toString()
									+ eol;
							seq--;
						}
					}
					if (showTime) {
						finalResponse += OTPdroid.this
								.getString(R.string.generated_in)
								+ " "
								+ (System.currentTimeMillis() - startTime)
								/ 1000F
								+ " "
								+ OTPdroid.this.getString(R.string.seconds);
					}
					return finalResponse;
				} catch (Exception e) {
					return null;
				}
			}

			@Override
			protected void onPreExecute() {
				this.d = ProgressDialog.show(OTPdroid.this, null,
						OTPdroid.this.getString(R.string.please_wait_), false);

				if (OTPdroid.this.miCalc != null) {
					OTPdroid.this.miCalc.setEnabled(false);
				}
			}

			@Override
			protected void onPostExecute(final String result) {
				if (result == null) {
					OTPdroid.this.response.setText(R.string.error_input);
				} else {
					OTPdroid.this.response.setText(result);
				}
				if (OTPdroid.this.miCalc != null) {
					OTPdroid.this.miCalc.setEnabled(true);
				}
				if (this.d != null) {
					this.d.cancel();
				}
			}

		} // .
		.execute((Void) null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected final void onStop() {
		super.onStop();

		final SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);
		final Editor editor = settings.edit();

		final boolean savePassphrase = settings.getBoolean(
				OTPdroid.PREF_SAVEPASSPHRASE, false);
		final boolean saveSequence = settings.getBoolean(
				OTPdroid.PREF_SAVECHALLENGE, false);

		this.savePassphrase(editor, savePassphrase, saveSequence);

		editor.commit();
	}

	/**
	 * Load passphrase from preferences.
	 * 
	 * @param settings
	 *            preferences
	 */
	private void loadPassphrase(final SharedPreferences settings) {
		AES aes;
		int l;

		aes = new AES();
		aes.genKey((this.imei + this.simid).getBytes());
		aes.setInputBase64(settings
				.getString(OTPdroid.PREF_SAVEDPASSPHRASE, ""));
		l = aes.decrypt();
		if (l > 0) {
			this.passphrase.setText(new String(aes.getOutput()));
			this.sequence.requestFocus();
		}

		aes = new AES();
		aes.genKey((this.imei + this.simid).getBytes());
		aes.setInputBase64(settings
				.getString(OTPdroid.PREF_SAVEDHASHMETHOD, ""));
		l = aes.decrypt();
		if (l > 0) {
			this.hashMethod.setSelection(Integer.parseInt(new String(aes
					.getOutput())));
		}

		aes = new AES();
		aes.genKey((this.imei + this.simid).getBytes());
		aes.setInputBase64(settings.getString(OTPdroid.PREF_SAVEDSEQUENCE, ""));
		l = aes.decrypt();
		if (l > 0) {
			this.sequence.setText(new String(aes.getOutput()));
			this.challenge.requestFocus();
		}

		aes = new AES();
		aes.genKey((this.imei + this.simid).getBytes());
		aes.setInputBase64(settings.getString(OTPdroid.PREF_SAVEDCHALENGE, ""));
		l = aes.decrypt();
		if (l > 0) {
			this.challenge.setText(new String(aes.getOutput()));
		}
	}

	/**
	 * Save passphrase to preferences.
	 * 
	 * @param savePassphrase
	 *            save passphase
	 * @param saveChallange
	 *            save challange
	 * @param editor
	 *            editor
	 */
	private void savePassphrase(final Editor editor,
			final boolean savePassphrase, final boolean saveChallange) {
		if (!savePassphrase && !saveChallange) {
			editor.remove(PREF_SAVEDPASSPHRASE);
			editor.remove(PREF_SAVEDHASHMETHOD);
			editor.remove(PREF_SAVEDSEQUENCE);
			editor.remove(PREF_SAVEDCHALENGE);
			return;
		}
		AES aes;
		int l;

		if (savePassphrase) {
			aes = new AES();
			aes.genKey((this.imei + this.simid).getBytes());
			aes.setInput(this.passphrase.getText().toString().getBytes());
			l = aes.encrypt();
			if (l > -1) {
				editor.putString(PREF_SAVEDPASSPHRASE, aes.getOutputBase64());
			}
		} else {
			editor.remove(PREF_SAVEDPASSPHRASE);
		}

		if (saveChallange) {
			aes = new AES();
			aes.genKey((this.imei + this.simid).getBytes());
			aes.setInput((this.hashMethod.getSelectedItemId() + "").getBytes());
			l = aes.encrypt();
			if (l > -1) {
				editor.putString(PREF_SAVEDHASHMETHOD, aes.getOutputBase64());
			}

			aes = new AES();
			aes.genKey((this.imei + this.simid).getBytes());
			aes.setInput(this.sequence.getText().toString().getBytes());
			l = aes.encrypt();
			if (l > -1) {
				editor.putString(PREF_SAVEDSEQUENCE, aes.getOutputBase64());
			}

			aes = new AES();
			aes.genKey((this.imei + this.simid).getBytes());
			aes.setInput(this.challenge.getText().toString().getBytes());
			l = aes.encrypt();

			if (l > -1) {
				editor.putString(PREF_SAVEDCHALENGE, aes.getOutputBase64());
			}
		} else {
			editor.remove(PREF_SAVEDHASHMETHOD);
			editor.remove(PREF_SAVEDSEQUENCE);
			editor.remove(PREF_SAVEDCHALENGE);
		}
	}

	/**
	 * Load IMEI and SIMID from phone.
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
		switch (id) {
		case DIALOG_ABOUT:
			builder = new AlertDialog.Builder(this);
			builder.setTitle(this.getString(R.string.about_) + " v"
					+ this.getString(R.string.app_version));
			builder.setPositiveButton(android.R.string.ok, null);
			builder.setView(View.inflate(this, R.layout.about, null));
			return builder.create();
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
