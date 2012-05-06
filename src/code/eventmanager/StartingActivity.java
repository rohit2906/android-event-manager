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

		// Check if an active session or the login data are present
		if (app.checkAccountAndLogin(false)) {
			Log.d(TAG, "Starting events activity");
			startActivity(new Intent(this, EventsActivity.class));
		} else {
			Log.d(TAG, "No credentials found. Starting login activity");
			startActivity(new Intent(this, LoginActivity.class));
		}

		// Close the starting activity
		finish();
	}

}
