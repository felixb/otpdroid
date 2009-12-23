package de.ub0r.android.otpdroid;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

public class Preferences extends PreferenceActivity implements BeerLicense {
	@Override
	public boolean onPreferenceTreeClick(
			final PreferenceScreen preferenceScreen, final Preference preference) {
		boolean savePassphrase = preference.getSharedPreferences().getBoolean(
				preference.getKey(), false);

		SharedPreferences settings = this
				.getSharedPreferences(Opiekey.PREFS, 0);
		SharedPreferences.Editor editor = settings.edit();

		editor.putBoolean(Opiekey.PREF_SAVE, savePassphrase);
		editor.commit();

		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.addPreferencesFromResource(R.layout.preferences);
	}
}
