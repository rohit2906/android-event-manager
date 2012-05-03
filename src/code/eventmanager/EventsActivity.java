package code.eventmanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Manage the events displaying
 */
public class EventsActivity extends Activity implements OnClickListener, OnItemLongClickListener, OnItemClickListener {

	private static final String TAG = EventsActivity.class.getSimpleName();
	static final String SEND_EVENTS_NOTIFICATIONS = "code.eventmanager.SEND_EVENTS_NOTIFICATIONS";
	private static final int NEW_EVENT_ACTIVITY_CODE = 0;
	private static final int DETAILS_ACTIVITY_DELETED_EVENT_CODE = 1;

	public static final String EVENT_DETAILS_ID = "EVENT_DETAILS_ID";
	public static final String EVENT_DETAILS_NAME = "EVENT_DETAILS_NAME";
	public static final String EVENT_DETAILS_ADDRESS = "EVENT_DETAILS_ADDRESS";
	public static final String EVENT_DETAILS_DESCRIPTION = "EVENT_DETAILS_DESCRIPTION";
	public static final String EVENT_DETAILS_CREATOR = "EVENT_DETAILS_CREATOR";
	public static final String EVENT_DETAILS_STARTING = "EVENT_DETAILS_STARTING";
	public static final String EVENT_DETAILS_ENDING = "EVENT_DETAILS_ENDING";

	Button buttonNewEvent;
	Intent pollerServiceIntent;
	EventManagerApp app;
	ListView eventsList;
	IntentFilter filter;
	EventsReceiver receiver;

	Cursor cursor;
	SimpleCursorAdapter adapter;
	static final String[] FROM = { DbHelper.EVENT_NAME, DbHelper.EVENT_ADDRESS, DbHelper.EVENT_STARTING_TS };
	static final int[] TO = { R.id.rowName, R.id.rowAddress, R.id.rowStartingTime };

	/**
	 * Reference to widgets and registration to onClick listener. Set the alarm
	 * for the service. Set the filter for receiving the notification from the
	 * service.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.events_layout);
		Log.i(TAG, "onCreate");
		app = (EventManagerApp) getApplication();

		eventsList = (ListView) findViewById(R.id.eventsList);
		eventsList.setOnItemClickListener(this);
		eventsList.setOnItemLongClickListener(this);

		buttonNewEvent = (Button) findViewById(R.id.eventsButtonNewEvent);
		buttonNewEvent.setOnClickListener(this);

		// set the alarm for the PollerService
		app.setAlarmForPollerFromPreferences();

		Log.d(TAG, "Set the receiver and the filter");
		receiver = new EventsReceiver();
		filter = new IntentFilter(PollerService.NEW_EVENTS_INTENT);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.eventsButtonNewEvent:
			startActivityForResult(new Intent(this, NewEventActivity.class), NEW_EVENT_ACTIVITY_CODE);
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == NEW_EVENT_ACTIVITY_CODE) {
			if (resultCode == RESULT_OK)
				Toast.makeText(this, "Event created", Toast.LENGTH_LONG).show();
			else if (resultCode == RESULT_CANCELED)
				Toast.makeText(this, "Problems with the creation of the event. Try again.", Toast.LENGTH_LONG).show();
		} else if (requestCode == DETAILS_ACTIVITY_DELETED_EVENT_CODE) {
			if (resultCode == DetailsEventActivity.CODE_EVENT_DELETED) {
				setupList();
				Toast.makeText(this, "Event deleted", Toast.LENGTH_LONG).show();
			}
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
			startActivityForResult(new Intent(this, NewEventActivity.class),
					NEW_EVENT_ACTIVITY_CODE);
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
		this.setupList();
		super.registerReceiver(receiver, filter, SEND_EVENTS_NOTIFICATIONS, null);
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

	/**
	 * Responsible for fetching data and setting up the list and the adapter
	 */
	private void setupList() {
		// Get the data
		cursor = app.getAllEvents();
		startManagingCursor(cursor);

		// Setup Adapter
		adapter = new SimpleCursorAdapter(this, R.layout.row, cursor, FROM, TO);
		adapter.setViewBinder(VIEW_BINDER);
		eventsList.setAdapter(adapter);
	}

	/**
	 * New ViewBinder in order to override the setViewValue method to display
	 * the time.
	 */
	static final ViewBinder VIEW_BINDER = new ViewBinder() {

		@Override
		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			//check if the view to bind is one of the time textview widgets
			if ((view.getId() != R.id.rowStartingTime))
				return false;

			// convert the timestamp into a real time way
			long timestamp = cursor.getLong(columnIndex);
			CharSequence realTime = DateUtils.getRelativeTimeSpanString(
					view.getContext(), timestamp);
			((TextView) view).setText(realTime);

			return true;
		}
	};

	class EventsReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "onReceived");
			int receivedEvents = intent.getIntExtra(PollerService.NEW_EVENTS_EXTRA_COUNT, 0);

			if (receivedEvents < 0) {
				// Impossible to get the spreadsheet. Show a toast
				Toast.makeText(EventsActivity.this, "Impossible to get the spreadsheet. Have you set the account?", Toast.LENGTH_LONG).show();
				Log.w(TAG, "Broadcast with -1 count. Impossible to get spreadsheets");
			} else if (receivedEvents > 0) {
				Log.v(TAG, receivedEvents + " events received");
				setupList();
			}
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> a, View v, int position, long id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Are you sure you want to delete it?").setCancelable(false)
		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {

				// TODO: check the creator
				app.deleteEvent(id);
				setupList();
			}
		})
		.setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		builder.show();
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> a, View v, int position, long id) {

		Cursor cursor = ((SimpleCursorAdapter) a.getAdapter()).getCursor();
		cursor.moveToPosition(position);
		Intent intent = new Intent(this, DetailsEventActivity.class);
		intent.putExtra(EVENT_DETAILS_ID, (int) id);
		intent.putExtra(EVENT_DETAILS_NAME, cursor.getString(1));
		intent.putExtra(EVENT_DETAILS_ADDRESS, cursor.getString(2));
		intent.putExtra(EVENT_DETAILS_DESCRIPTION, cursor.getString(3));
		intent.putExtra(EVENT_DETAILS_CREATOR, cursor.getString(4));
		intent.putExtra(EVENT_DETAILS_STARTING, app.timestamp2Date(cursor.getLong(5)));
		intent.putExtra(EVENT_DETAILS_ENDING, app.timestamp2Date(cursor.getLong(6)));
		startActivityForResult(intent, DETAILS_ACTIVITY_DELETED_EVENT_CODE);
	}
}
