package code.eventmanager;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

public class PollerService extends IntentService {

	private static final String TAG = PollerService.class.getSimpleName();

	public static final String NEW_EVENTS_INTENT = "code.eventmanager.NEW_EVENTS";
	public static final String NEW_EVENTS_EXTRA_COUNT = "NEW_EVENTS_EXTRA_COUNT";
	public static final String RECEIVE_EVENTS_NOTIFICATIONS = "code.eventmanager.RECEIVE_EVENTS_NOTIFICATION";

	private NotificationManager notificationManager;
	private Notification notification;

	public PollerService() {
		super(TAG);
		Log.d(TAG, TAG + " constructed");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(TAG, "onHandleIntent");
		Intent newEventsIntent = null;

		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notification = new Notification(android.R.drawable.stat_sys_download_done, "", 0);		

		EventManagerApp app = (EventManagerApp) getApplication();
		int newEvents = app.parseEvents();
		if (newEvents == 0) {
			Log.d(TAG, "No new events");
		} else if (newEvents > 0) {
			Log.d(TAG, newEvents + " new events found.");

			// Create the intent to broadcast
			newEventsIntent = new Intent(NEW_EVENTS_INTENT);
			newEventsIntent.putExtra(NEW_EVENTS_EXTRA_COUNT, newEvents);

			// Create the notification
			sendNewEventsNotificationToSystemBar(newEvents);
		} else if (newEvents < 0) {
			Log.w(TAG, "Impossible to get the spreadsheets. Have you set the account?");

			// Create the intent to broadcast reporting the error (-1)
			newEventsIntent = new Intent(NEW_EVENTS_INTENT);
			newEventsIntent.putExtra(NEW_EVENTS_EXTRA_COUNT, -1);
		}

		// Send the Broadcast
		if (newEventsIntent != null) {
			sendBroadcast(newEventsIntent, RECEIVE_EVENTS_NOTIFICATIONS);
		}
	}

	/**
	 * Creates a notification in the notification bar telling user there are new events
	 * 
	 * @param newEventsCount number of new events
	 */
	private void sendNewEventsNotificationToSystemBar(int newEventsCount) {
		Log.d(TAG, "sendNewEventsNotificationToSystemBar");
		
		// Create an intent that will lead us to the EventsActivity when the user
		// clicks on the notification
		PendingIntent pendingIntent = PendingIntent.getActivity(this, -1,
				new Intent(this, EventsActivity.class),
				PendingIntent.FLAG_UPDATE_CURRENT);
		
		// Set the time shown on the notification
		notification.when = System.currentTimeMillis();
		
		// Cancel this notification as soon as the user clicks on it
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		
		// Set the title and the summary
		CharSequence notificationTitle = getText(R.string.notificationTitle);
		CharSequence notificationSummary = createNotificationSummaryString(newEventsCount);
		
		// Set the information in the notification 
		notification.setLatestEventInfo(this, notificationTitle, notificationSummary, pendingIntent);
		notificationManager.notify(0, notification);
	}
	
	/**
	 * Create the notification summary text based on the number of new events
	 * 
	 * @param newEventsCount number of new events
	 * @return A string containing the summary text for the notification
	 */
	private String createNotificationSummaryString(int newEventsCount) {
		return getString(R.string.notificationMessage, newEventsCount, (newEventsCount > 1) ? "s" : "");
	}
}
