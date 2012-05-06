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

	private static final int NEW_EVENT_ACTIVITY_REQUEST_CODE = 0;
	private static final int DETAILS_ACTIVITY_REQUEST_CODE = 1;

	public static final String SEND_EVENTS_NOTIFICATIONS = "code.eventmanager.SEND_EVENTS_NOTIFICATIONS";

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

		Log.d(TAG, "Set the receiver and the filter");
		receiver = new EventsReceiver();
		filter = new IntentFilter(PollerService.NEW_EVENTS_INTENT);

		// set the alarm for the PollerService
		app.setPollerAlarmFromPreferences();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.eventsButtonNewEvent:
			startActivityForResult(new Intent(this, NewEventActivity.class), NEW_EVENT_ACTIVITY_REQUEST_CODE);
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case NEW_EVENT_ACTIVITY_REQUEST_CODE:
			if (resultCode == NewEventActivity.RESULT_EVENT_CREATED)
				eventCreatedAction();
			else if (resultCode == NewEventActivity.RESULT_EVENT_ERROR)
				eventNotCreatedAction();
			break;

		case DETAILS_ACTIVITY_REQUEST_CODE:
			if (resultCode == DetailsActivity.RESULT_EVENT_DELETED)
				eventDeletedAction();
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
			startActivityForResult(new Intent(this, NewEventActivity.class),
					NEW_EVENT_ACTIVITY_REQUEST_CODE);
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

		// this permission is a requirement for anyone who wants to send us this type of broadcast.
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
				Toast.makeText(EventsActivity.this,
						getText(R.string.eventsToastImpossibleGetSpreadsheet),
						Toast.LENGTH_LONG).show();
				Log.w(TAG, "Broadcast with -1 count. Impossible to get spreadsheets");
			} else if (receivedEvents > 0) {
				Log.v(TAG, receivedEvents + " events received");
				setupList();
			}
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> a, View v, int position, final long rowId) {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
		alertDialog.setMessage(getText(R.string.alertDialogDeleteConfirmation));
		alertDialog.setCancelable(true);
		
		alertDialog.setPositiveButton(getText(R.string.yes), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				if (app.deleteEvent((int)rowId)) {
					eventDeletedAction();
					
					// Update the widget
					// We are not doing that in the eventDeletedAction() because
					// that one is not called if you are deleting an item from
					// a DetailsActivity called from the widget. So we have the
					// refresh in the DetailsActivity, but this is another way
					// to delete the event without using the DetailsActivity.
					sendBroadcast(new Intent(EventManagerWidget.REFRESH_WIDGET));
				} else {
					eventNotDeletedAction();
				}
			}
		});
		
		alertDialog.setNegativeButton(getText(R.string.no), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		
		alertDialog.show();
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> a, View v, int position, long id) {

		Cursor cursor = ((SimpleCursorAdapter) a.getAdapter()).getCursor();
		cursor.moveToPosition(position);
		Intent intent = new Intent(this, DetailsActivity.class);
		intent.putExtra(DetailsActivity.EVENT_DETAILS_ID, id);
		startActivityForResult(intent, DETAILS_ACTIVITY_REQUEST_CODE);
	}

	private void eventDeletedAction() {
		setupList();
		Toast.makeText(this, getText(R.string.toastEventDeleted), Toast.LENGTH_LONG).show();
	}

	private void eventNotDeletedAction() {
		Toast.makeText(this, getText(R.string.eventsToastProblemDeleting), Toast.LENGTH_LONG).show();
	}

	private void eventCreatedAction() {
		setupList();
		Toast.makeText(this, getText(R.string.toastEventCreated), Toast.LENGTH_LONG).show();
		sendBroadcast(new Intent(EventManagerWidget.REFRESH_WIDGET));
	}

	private void eventNotCreatedAction() {
		Toast.makeText(this, getText(R.string.toastProblemsCreating), Toast.LENGTH_LONG).show();
	}
}
