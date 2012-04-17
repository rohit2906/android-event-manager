package code.eventmanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class FirstActivity extends Activity {
	
	private static final String TAG = FirstActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "onCreate");
		EventManagerApp app=(EventManagerApp) getApplication();
		String email = app.getPrefs().getString(
				(String) getText(R.string.credentialsKeyCustomAccountMail), "");
		String password = app.getPrefs().getString(
				(String) getText(R.string.credentialsKeyCustomAccountPassword),
				"");
		boolean defaultAccount = app.getPrefs().getBoolean(
				(String) getText(R.string.credentialsKeyDefaultAccount), false);
		if (email == "" && password == "" && defaultAccount == false) {
			Log.d(TAG, "No credentials found. Starting login activity");
			startActivity(new Intent(this, LoginActivity.class));
		} else {
			app.setAccount();
			Log.d(TAG, "Credentials found. Starting events activity");
			startActivity(new Intent(this, EventsActivity.class));
		}
	}

}
