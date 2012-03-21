package code.eventmanager;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.util.Log;

public class EventManagerApp extends Application implements
		OnSharedPreferenceChangeListener {
	private static final String TAG = EventManagerApp.class.getSimpleName();
	private SharedPreferences prefs;

	@Override
	public void onCreate() {
		super.onCreate();
		this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
		this.prefs.registerOnSharedPreferenceChangeListener(this);
		Log.i(TAG, "onCreated");
		startActivity(new Intent(this, LoginActivity.class));
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		Log.i(TAG, "onTerminated");
	}

	public synchronized void onSharedPreferenceChanged(
			SharedPreferences sharedPreferences, String key) {
	}

	public SharedPreferences getPrefs() {
		return prefs;
	}
}
