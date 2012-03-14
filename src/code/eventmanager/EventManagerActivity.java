package code.eventmanager;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class EventManagerActivity extends Activity {

	@SuppressWarnings("unused")
	private static final String TAG = "EventManagerActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if (prefs.getString("username", "").isEmpty() || prefs.getString("password", "").isEmpty())
			startActivity(new Intent(this, LoginActivity.class));
		else
			startActivity(new Intent(this, EventsActivity.class));
	}
}
