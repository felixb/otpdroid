package de.ub0r.android.otpdroid;

import android.os.Bundle;
import android.os.Build.VERSION_CODES;
import android.preference.PreferenceActivity;
import android.view.MenuItem;
import de.ub0r.android.lib.Utils;

/**
 * Preferences.
 * 
 * @author flx
 */
public class Preferences extends PreferenceActivity {
	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void onCreate(final Bundle savedInstanceState) {
		if (Utils.isApi(VERSION_CODES.HONEYCOMB)) {
			this.setTheme(R.style.Theme_SherlockUb0r);
		}
		super.onCreate(savedInstanceState);
		this.addPreferencesFromResource(R.xml.prefs);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
			return true;
		default:
			return false;
		}
	}
}
