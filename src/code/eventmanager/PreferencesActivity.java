package code.eventmanager;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.widget.Toast;

public class PreferencesActivity extends PreferenceActivity {

	private static final String TAG = PreferencesActivity.class.getSimpleName();

	EventManagerApp app;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences_layout);
		Log.v(TAG, "onCreate");

		app = (EventManagerApp) getApplication();
	}

	/**
	 * If back button is pressed,check if there is an account set. Otherwise launch a toast. 
	 */
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Log.v(TAG, "onBackPressed");
		String username = app.getPrefs().getString(
				(String) getText(R.string.preferencesKeyCustomAccountMail), "");
		String password = app.getPrefs().getString(
				(String) getText(R.string.preferencesKeyCustomAccountPassword),
				"");
		boolean defaultAccount = app.getPrefs().getBoolean(
				(String) getText(R.string.preferencesKeyDefaultAccount), true);
		if (defaultAccount == false)
			if (password == "" || username == "") {
				Toast.makeText(this, "No account set", Toast.LENGTH_LONG)
						.show();
			}
	}
}
