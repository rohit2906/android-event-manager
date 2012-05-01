package code.eventmanager;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.SimpleCursorAdapter.ViewBinder;

/**
 * Manage the events displaying
 */
public class EventsActivity extends Activity implements OnClickListener {

	private static final String TAG = EventsActivity.class.getSimpleName();

	Button buttonNewEvent;
	Intent pollerServiceIntent;
	EventManagerApp app;
	ListView eventList;

//	SQLiteDatabase db;

	IntentFilter filter;
	EventsReceiver receiver;

	Cursor cursor;
	SimpleCursorAdapter adapter;

	/**
	 * Reference to widgets and registration to onClick listener. Set the alarm
	 * for the service. Set the filter for receiving the notification from the
	 * service. Open the db.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.events_layout);
		
		Log.i(TAG, "onCreate");

		app = (EventManagerApp) getApplication();
		// pollerServiceIntent = null;
		// startPoller();

		eventList = (ListView) findViewById(R.id.eventsList);
		buttonNewEvent = (Button) findViewById(R.id.eventsButtonNewEvent);
		buttonNewEvent.setOnClickListener(this);

		// set the alarm for the PollerService
		app.setAlarm4Poller();

		Log.d(TAG, "Set the receiver and the filter");
		receiver = new EventsReceiver();
		filter = new IntentFilter(PollerService.NEW_EVENTS_INTENT);
		

//		// Open the db in readable mode
//		Log.d(TAG, "Open the database");
//		db = app.getDbHelper().getReadableDatabase();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.eventsButtonNewEvent:
			startActivity(new Intent(this, NewEventActivity.class));
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.events_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuItemPreferences:
			startActivity(new Intent(this, PreferencesActivity.class));
			break;
		case R.id.menuItemSyncNow:
			startService(new Intent(this, PollerService.class));
			break;
		case R.id.menuItemNewEvent:
			startActivity(new Intent(this, NewEventActivity.class));
			break;
		}
		return true;
	}

	/**
	 * Refresh the event list and register for the notifications
	 */
	@Override
	protected void onResume() {
		super.onResume();
		Log.v(TAG, "onResume");
		super.registerReceiver(receiver, filter);
	}

	/**
	 * Unregister for the notifications
	 */
	@Override
	protected void onPause() {
		super.onPause();
		Log.v(TAG, "onPause");
		unregisterReceiver(receiver);
	}
	
//	/**
//	 * Close the database
//	 */
//	@Override
//	protected void onDestroy() {
//		Log.v(TAG, "onDestroy");
//		db.close();
//		super.onDestroy();
//	}

	/**
	 * Responsible for fetching data and setting up the list and the adapter
	 */
	private void setupList() {
		// Get the data
		SQLiteDatabase db = app.getDbHelper().getReadableDatabase();
		try {
		cursor = db.query(DbHelper.TABLE_EVENTS, null, null, null, null, null,
				DbHelper.EVENTS_STARTING_TS);
		startManagingCursor(cursor);

		// Setup Adapter
		adapter.setViewBinder(VIEW_BINDER);
		eventList.setAdapter(adapter);
		} finally {
			db.close();
		}
	}

	/**
	 * New ViewBinder in order to override the setViewValue method to display the time.
	 */
	static final ViewBinder VIEW_BINDER = new ViewBinder() {

		@Override
		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			
			//check if the view to bind is one of the time textview widgets
			if (view.getId() != R.id.eventRowTvStarting
					&& view.getId() != R.id.eventRowTvEnding)
				return false;

			//convert the timestamp into a real time way
			long timestamp = cursor.getLong(columnIndex);
			CharSequence realTime = DateUtils.getRelativeTimeSpanString(
					view.getContext(), timestamp);
			((TextView) view).setText(realTime);

			return true;
		}
	};

	/*
	 * private void startPoller() { if (pollerServiceIntent == null) {
	 * pollerServiceIntent = new Intent(this, PollerServiceOld.class);
	 * startService(pollerServiceIntent); } else { Log.d(TAG,
	 * "startPoller(): PollerService is already running."); } }
	 * 
	 * private void stopPoller() { if (pollerServiceIntent != null) {
	 * stopService(pollerServiceIntent); pollerServiceIntent = null; } else {
	 * Log.d(TAG, "stopPoller(): PollerService is not running."); } }
	 */

	class EventsReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(EventsReceiver.class.getSimpleName(), "onReceived");
			setupList();
		}
	}
}
