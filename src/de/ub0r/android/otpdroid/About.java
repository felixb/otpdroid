package de.ub0r.android.otpdroid;

import android.app.Activity;
import android.app.AlertDialog;

public class About implements BeerLicense {
	AlertDialog alert;

	public About(final Activity activity) {
		this.alert = new AlertDialog.Builder(activity).create();

		this.init();
	}

	public void init() {
		this.alert.setCanceledOnTouchOutside(true);
		this.alert.setCancelable(true);
		this.alert.setTitle("About");
		this.alert
				.setMessage("Program to compute OTP challenges.\n\nAuthor: android@f00d.nl\n\nInspired and based on jotp (http://www.cs.umd.edu/~harry/jotp/)\n\nLicensed under:\n"
						+ de.ub0r.android.otpdroid.BeerLicense.BeerLicense);
	}

	public void show() {
		this.alert.show();
	}
}
