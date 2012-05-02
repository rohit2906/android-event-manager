package code.eventmanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class StartingActivity extends Activity {

	private static final String TAG = StartingActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "onCreate");
		
		EventManagerApp app = (EventManagerApp) getApplication();
		String email = app.getPrefs().getString(
				(String) getText(R.string.preferencesKeyCustomAccountMail), "");
		String password = app.getPrefs().getString(
				(String) getText(R.string.preferencesKeyCustomAccountPassword), "");
		boolean defaultAccount = app.getPrefs().getBoolean(
				(String) getText(R.string.preferencesKeyDefaultAccount), false);
		
		if (defaultAccount == false && (email.isEmpty() || password.isEmpty())) {
			Log.d(TAG, "No credentials found. Starting login activity");
			startActivity(new Intent(this, LoginActivity.class));
		} else {
			if (defaultAccount == true)
				app.setDefaultAccount();
			else
				app.setAnotherAccount(email, password);
			
			Log.d(TAG, "Credentials found. Starting events activity");
			startActivity(new Intent(this, EventsActivity.class));
		}
		
		// Close the starting activity
		finish();
	}

}
