package code.eventmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

public class NetworkReceiver extends BroadcastReceiver {

	public static final String TAG = NetworkReceiver.class.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {

		EventManagerApp app = (EventManagerApp) context.getApplicationContext();

		// Enable this feature only if the minutes between updates is less than 120
		// otherwise we will risk to never update the phone.
		int minutes = app.getMinutesBetweenUpdates();
		if (minutes < 120)
		{

			// When the system broadcasts the particular intent action that this receiver subscribes,
			// the intent will have an extra piece of information indicating whether the network is up or down.
			boolean isNetworkDown = intent.getBooleanExtra(
					ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);

			if (isNetworkDown) {
				Log.d(TAG, "onReceive: NOT connected, stopping PollerService");
				app.setPollerAlarm(EventManagerApp.ALARM_DISABLED);
			} else {
				Log.d(TAG, "onReceive: connected, starting PollerService");
				app.setPollerAlarm(minutes);
			}
		}
	}

}
