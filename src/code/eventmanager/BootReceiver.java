package code.eventmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

	private static final String TAG = BootReceiver.class.getSimpleName();

	public void onReceive(Context context, Intent callingIntent) {
		
		Log.d(TAG, "onReceive");

		EventManagerApp app = (EventManagerApp) context.getApplicationContext();

		// Check if the start on boot is enabled
		if (!app.getStartOnBoot()) {
			Log.d(TAG, "Start on Boot not enabled");
			return;
		}

		// Check if the automatic updates are enabled
		int minutes = app.getMinutesBetweenUpdates();
		if (minutes == EventManagerApp.ALARM_DISABLED) {
			Log.d(TAG, "Automatic updates not enabled");
			return;
		}
		
		// Check if there are login data available
		if (app.checkAccountAndLogin(true)) {
			Log.d(TAG, "Login Data Available. Starting Updater");
			app.setPollerAlarm(minutes);
		} else {
			Log.w(TAG, "StartOnBoot requested but no login data available");
		}
	}
}
