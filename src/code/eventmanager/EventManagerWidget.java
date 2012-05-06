package code.eventmanager;

import java.util.Date;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.RemoteViews;

public class EventManagerWidget extends AppWidgetProvider {
	private static final String TAG = EventManagerWidget.class.getSimpleName();
	
	public static final String REFRESH_WIDGET = "code.eventmanager.REFRESH_WIDGET";
	
	private static PendingIntent pendingIntent;

	/**
	 * This method is called whenever our widget is to be updated, the frequency is specified in the AppWidgetProviderInfo
	 */
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		// find the first "not yet finished" event
		Cursor cursor = context.getContentResolver().query(EventProvider.CONTENT_URI,
				null, DbHelper.EVENT_ENDING_TS + " > ?",
				new String[] { Long.toString((new Date()).getTime()) },
				DbHelper.EVENT_STARTING_TS + " DESC");
		try {
			CharSequence name = context.getText(R.string.eventManagerWidgetNoEvents);
			CharSequence startingTime = "";
			CharSequence address = "";

			if (cursor.moveToFirst()) {
				name = cursor.getString(cursor.getColumnIndex(DbHelper.EVENT_NAME));
				startingTime = DateUtils.getRelativeTimeSpanString(context, cursor
						.getLong(cursor.getColumnIndex(DbHelper.EVENT_STARTING_TS)));
				address = cursor.getString(cursor.getColumnIndex(DbHelper.EVENT_ADDRESS));
				long id = cursor.getLong(cursor.getColumnIndex(DbHelper.EVENT_ID));
				Intent intent = new Intent(context, DetailsActivity.class);
				intent.putExtra(DetailsActivity.EVENT_DETAILS_ID, id);
				pendingIntent = PendingIntent.getActivity(context, -1, intent,
						PendingIntent.FLAG_UPDATE_CURRENT);
			} else if (pendingIntent != null) {
				pendingIntent.cancel();
			}

			// Loop through all user's instances of this widget (we put the same
			// data in every instance)
			for (int appWidgetId : appWidgetIds) {
				Log.d(TAG, "Updating widget " + appWidgetId);

				// Since the view representing our widget is in another process,
				// (the widgets run in the Home application), we use the RemoteViews constructor,
				// that is a shared memory system designed specifically for widgets.
				RemoteViews views = new RemoteViews(context.getPackageName(),
						R.layout.eventmanager_widget);

				// Update the View
				views.setTextViewText(R.id.rowName, name);
				views.setTextViewText(R.id.rowStartingTime, startingTime);
				views.setTextViewText(R.id.rowAddress, address);

				// Create the intent to launch when the icon is clicked
				if (pendingIntent != null)
					views.setOnClickPendingIntent(R.id.eventIcon, pendingIntent);

				// Inform the system to update our widget. This will happen
				// asynchronously, but shortly after onUpdate() completes
				appWidgetManager.updateAppWidget(appWidgetId, views);
			}
		} finally {
			// good practice
			cursor.close();
		}
		Log.d(TAG, "onUpdated");
	}

	// The call to onReceive() is not necessary in a typical widget. But since a widget is a
	// broadcast receiver, and since our Updater service does send a broadcast when we
	// get a new status update, this method is a good opportunity to invoke onUpdate()
	// and get the latest status data updated on the widget.

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);

		// Check whether the intent was for the new status broadcast
		if (intent.getAction().equals(PollerService.NEW_EVENTS_INTENT) || 
			intent.getAction().equals(REFRESH_WIDGET)) {
			Log.d(TAG, "onReceived detected new status update");
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			this.onUpdate(context, appWidgetManager, appWidgetManager
					.getAppWidgetIds(new ComponentName(context, EventManagerWidget.class)));
		}
	}
}
