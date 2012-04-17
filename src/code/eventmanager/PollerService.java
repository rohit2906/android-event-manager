package code.eventmanager;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class PollerService extends IntentService {

	private static final String TAG = PollerService.class.getSimpleName();

	public static final String NEW_EVENTS_INTENT = "code.eventmanager.NEW_STATUS";
	public static final String NEW_EVENTS_EXTRA_COUNT = "NEW_STATUS_EXTRA_COUNT";
	public static final String RECEIVE_EVENTS_NOTIFICATIONS = "code.eventmanager.RECEIVE_EVENTS_NOTIFICATIONS";

	public PollerService() {
		super(TAG);
		Log.d(TAG, TAG + " constructed");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(TAG, "onHandleIntent");
		EventManagerApp app = (EventManagerApp) getApplication();
		int news=app.parseEvents();
		if (news > 0) {
			Log.d(TAG, "New events found");
			Log.v(TAG, news + " new events");
			Intent newEventIntent = new Intent(NEW_EVENTS_INTENT);
			sendBroadcast(newEventIntent, RECEIVE_EVENTS_NOTIFICATIONS);
		}else
			Log.d(TAG, "No new events");
	}
}
