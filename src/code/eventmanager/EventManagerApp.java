package code.eventmanager;

import java.util.ArrayList;
import java.util.Calendar;
import code.eventmanager.auth.AndroidAuthenticator;
import com.pras.SpreadSheet;
import com.pras.SpreadSheetFactory;
import com.pras.WorkSheet;
import com.pras.WorkSheetCell;
import com.pras.WorkSheetRow;

import android.accounts.AccountManager;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.Toast;

/**
 * 
 * Application class in order to share all information that are needed to the
 * other activities
 */
public class EventManagerApp extends Application implements
		OnSharedPreferenceChangeListener {

	private static final String TAG = EventManagerApp.class.getSimpleName();

	private SharedPreferences prefs;
	private SpreadSheetFactory spreadsheetFactory;
	private AlarmManager alarmManager;
	private int lastSavedEventId = -1;
	private String spreadSheetKey;
	public DbHelper dbHelper;

	private static final int ALARM_DISABLED = 0;

	/**
	 * Reference to preferences and registration to their changes. Also instance
	 * the intent for the poller service and get an instance of the system alarm
	 * manager.
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "onCreated");
		this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
		this.prefs.registerOnSharedPreferenceChangeListener(this);
		dbHelper = new DbHelper(this);

		// Get the alarm service from the system
		alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		// TODO remove later or put a preference like "run in background
		setPollerAlarm(ALARM_DISABLED);
		Log.i(TAG, "onTerminated");
	}

	/**
	 * When the default account checkbox in the preferences change, it will
	 * reset the account
	 */
	public synchronized void onSharedPreferenceChanged(
			SharedPreferences sharedPreferences, String key) {
		Log.i(TAG, "onSharedPreferenceChanged");

		if (key == getText(R.string.preferencesKeyDefaultAccount)) {
			Log.d(TAG, "Change default account checkbox");
			spreadsheetFactory = null;
			boolean defaultAccount = prefs.getBoolean(
					getText(R.string.preferencesKeyDefaultAccount).toString(),
					false);
			if (defaultAccount == true) {
				setDefaultAccount();
			} else {
				// check if user e-mail and password are emptyString email =
				// prefs.getString(
				String email = prefs
						.getString(
								(String) getText(R.string.preferencesKeyCustomAccountMail),
								"");
				String password = prefs
						.getString(
								(String) getText(R.string.preferencesKeyCustomAccountPassword),
								"");

				if (email.isEmpty() || password.isEmpty())
					Log.i(TAG, "No account set");
				else
					setAnotherAccount(email, password);
			}
		}

		// check if the user type a new username or password for the custom
		// account
		else if (key == getText(R.string.preferencesKeyCustomAccountMail)
				|| key == getText(R.string.preferencesKeyCustomAccountPassword)) {
			String email = prefs.getString(
					(String) getText(R.string.preferencesKeyCustomAccountMail),
					"");
			String password = prefs
					.getString(
							(String) getText(R.string.preferencesKeyCustomAccountPassword),
							"");

			if (email != "" && password != "")
				setAnotherAccount(email, password);
		}

		else if (key == getText(R.string.preferencesKeyMinutesBetweenUpdates)) {
			Log.d(TAG, "Change update timer");
			setAlarmForPollerFromPreferences();
		}
	}

	/**
	 * Get the reference to the preferences
	 * 
	 * @return reference to preferences
	 */
	public SharedPreferences getPrefs() {
		return prefs;
	}

	/**
	 * Get the reference to the spreadsheetFactory
	 * 
	 * @return reference to spreadsheetFactory
	 */
	public SpreadSheetFactory getSpreadsheetFactory() {
		return spreadsheetFactory;
	}

	/**
	 * Allocate a new SpreadSheetFactory for default account on the phone
	 */
	public void setDefaultAccount() {
		Log.d(TAG, "Logging in with default account.");
		spreadsheetFactory = SpreadSheetFactory
				.getInstance(new AndroidAuthenticator(getApplicationContext()));
	}

	/**
	 * Allocate a new SpreadSheetFactory for the new custom account
	 * 
	 * @param email
	 *            username
	 * @param password
	 *            password
	 */
	public void setAnotherAccount(String email, String password) {
		Log.d(TAG, "Setting up custom account");
		Log.v(TAG, "Email: " + email);
		spreadsheetFactory = SpreadSheetFactory.getInstance(email, password);
	}

	/**
	 * Parse the events in the spreadsheet
	 * 
	 * @param spreadsheet
	 *            the spreadsheet with events
	 * @param worksheet
	 *            the spreadsheet with events
	 */
	public int parseEvents() {
		WorkSheet ws = getWorkSheet();
		ArrayList<WorkSheetRow> rows = ws.getData(false);
		Log.v(TAG, "Number of rows: " + rows.size());

		int maxId = lastSavedEventId;
		ContentValues record = new ContentValues();
		int newEvents = 0;

		for (int i = 0; i < rows.size(); i++) {
			ArrayList<WorkSheetCell> wsc = rows.get(i).getCells(); // get the
																	// cells in
																	// that row
			int id = Integer.parseInt(wsc.get(0).getValue());

			// Put in the database only the new events
			if (id > maxId) {
				maxId = id;

				// clear the database record
				record.clear();

				record.put(DbHelper.EVENT_ID, id);
				record.put(DbHelper.EVENT_NAME, wsc.get(1).getValue());
				record.put(DbHelper.EVENT_ADDRESS, wsc.get(2).getValue());
				record.put(DbHelper.EVENT_DESCRIPTION, wsc.get(3).getValue());
				record.put(DbHelper.EVENT_CREATOR, wsc.get(4).getValue());
				record.put(DbHelper.EVENT_STARTING_TS,
						Long.parseLong(wsc.get(5).getValue()));
				record.put(DbHelper.EVENT_ENDING_TS,
						Long.parseLong(wsc.get(6).getValue()));

				if (insertOrIgnore(record))
					newEvents++;
			}
		}

		lastSavedEventId = maxId;
		return newEvents;
	}

	/**
	 * Set the alarm for the poller getting the interval value from the
	 * preferences
	 */
	public void setAlarmForPollerFromPreferences() {
		Log.d(TAG, "Updating poller alarm");
		int minutes = Integer.parseInt(prefs.getString(
				getText(R.string.preferencesKeyMinutesBetweenUpdates)
						.toString().trim(), "1"));
		if (minutes >= 0)
			setPollerAlarm(minutes);
	}

	/**
	 * Set the poller alarm
	 * 
	 * @param minutes
	 *            the minutes between every launch
	 */
	private void setPollerAlarm(int minutes) {
		if (minutes < 0)
			return;

		Intent intent = new Intent(this, PollerService.class);
		PendingIntent pollerTriggerPendingIntent = PendingIntent.getService(
				getApplicationContext(), -1, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		if (minutes == ALARM_DISABLED) {
			alarmManager.cancel(pollerTriggerPendingIntent);
			Log.v(TAG, "Automatic updates disabled");
		} else {
			// set the alarm (it could be approximately).
			// RTC means that the alarm'll not wake up the device if it's
			// sleeping. The next
			// parameter specifies the time is in milliseconds.
			alarmManager.setInexactRepeating(AlarmManager.RTC,
					System.currentTimeMillis(), minutes * 60000,
					pollerTriggerPendingIntent);
			Log.v(TAG, "Alarm set every " + minutes + " minutes");
		}
	}

	/**
	 * Get the reference to the dbHelper of the application
	 * 
	 * @return reference to DbHelper
	 */
	public DbHelper getDbHelper() {
		return dbHelper;
	}

	/**
	 * Return all the events.
	 * 
	 * @return A cursor with all the events in the database.
	 */
	public Cursor getAllEvents() {
		return dbHelper.getAllEvents();
	}

	/**
	 * Insert the values in the database ignoring the conflicts.
	 * 
	 * @param values
	 *            the ContentValues to insert in the database.
	 * 
	 * @return true if inserted, false if ignored.
	 */
	public boolean insertOrIgnore(ContentValues values) {
		boolean valuesInserted = false;
		SQLiteDatabase db = getDbHelper().getWritableDatabase();

		try {
			db.insertWithOnConflict(DbHelper.TABLE_EVENTS, null, values,
					SQLiteDatabase.CONFLICT_IGNORE);
			Log.v(TAG, "Record " + values.getAsString(DbHelper.EVENT_ID)
					+ " added.");
			valuesInserted = true;
		} catch (SQLException e) {
			Log.v(TAG, "Record " + values.getAsString(DbHelper.EVENT_ID)
					+ " ignored.");
		} finally {
			db.close();
		}

		return valuesInserted;
	}

	/**
	 * Create and initialize the Spreadsheet and the Worksheet on Google Docs
	 * 
	 * @param spreadsheetTitle
	 */
	private void createWebSpreadSheet(String spreadsheetTitle) {
		getSpreadsheetFactory().createSpreadSheet(spreadsheetTitle);
		ArrayList<SpreadSheet> spreadsheets = getSpreadsheetFactory()
				.getSpreadSheet(spreadsheetTitle, false);

		String[] columns = new String[7];
		columns[0] = DbHelper.EVENT_ID;
		columns[1] = DbHelper.EVENT_NAME;
		columns[2] = DbHelper.EVENT_ADDRESS;
		columns[3] = DbHelper.EVENT_DESCRIPTION;
		columns[4] = DbHelper.EVENT_CREATOR;
		columns[5] = DbHelper.EVENT_STARTING_TS;
		columns[6] = DbHelper.EVENT_ENDING_TS;

		spreadsheets.get(0).addWorkSheet(spreadsheetTitle, columns);
		spreadsheets.get(0).deleteWorkSheet(
				spreadsheets.get(0).getAllWorkSheets().get(0));
	}

	/**
	 * Return the current maximum Event ID in the database.
	 * 
	 * @param db
	 *            an already opened database or null if you don't have one
	 *            already opened.
	 * @return the current maximum Event ID in the database or -1 if there are
	 *         no entries.
	 */
	public int getMaxEventId(SQLiteDatabase db) {
		boolean dbAlreadyOpen = true;

		if (db == null) {
			dbAlreadyOpen = false;
			db = getDbHelper().getReadableDatabase();
		}

		String query = "SELECT MAX(" + DbHelper.EVENT_ID + ") FROM "
				+ DbHelper.TABLE_EVENTS;

		Cursor cursor = db.rawQuery(query, null);
		int max = (cursor.moveToFirst() ? cursor.getInt(0) : -1);

		if (!dbAlreadyOpen)
			db.close();

		return max;
	}

	/**
	 * Convert a date into a timestamp
	 * 
	 * @param year
	 * @param month
	 * @param day
	 * @param hour
	 * @param minute
	 * 
	 * @return milliseconds
	 */
	public long date2Timestamp(int year, int month, int day, int hour,
			int minute) {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, month);
		c.set(Calendar.DAY_OF_MONTH, day);
		c.set(Calendar.HOUR, hour);
		c.set(Calendar.MINUTE, minute);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTimeInMillis();
	}

	public CharSequence timestamp2Date(long timestamp) {
		CharSequence realTime = DateUtils.getRelativeTimeSpanString(
				getApplicationContext(), timestamp);
		return realTime;
	}

	/**
	 * Check which account is in use and return the email of the user.
	 * 
	 * @return the email of the user
	 */
	public String getUsername() {
		String username;
		boolean checked = this.getPrefs()
				.getBoolean(
						getText(R.string.preferencesKeyDefaultAccount)
								.toString(), true);
		if (checked) {
			AccountManager manager = AccountManager
					.get(getApplicationContext());
			username = manager.getAccountsByType("com.google")[0].name;
		} else {
			username = this.getPrefs().getString(
					getText(R.string.preferencesKeyCustomAccountMail)
							.toString(), "");
		}
		return username;
	}

	/**
	 * Delete an event from the database and from the online spreadsheet
	 * 
	 * @param id
	 *            The id of the event
	 * @return the outcome of the operation
	 */
	public boolean deleteEvent(int id) {
		SQLiteDatabase db = getDbHelper().getWritableDatabase();
		String[] arrayId = new String[1];
		arrayId[0] = Integer.toString(id);
		String[] arrayCreator = new String[1];
		arrayCreator[0] = DbHelper.EVENT_CREATOR;
		Cursor cursor = db.rawQuery("SELECT " + DbHelper.EVENT_CREATOR
				+ " FROM " + DbHelper.TABLE_EVENTS + " WHERE "
				+ DbHelper.EVENT_ID + "=" + id, null);
		cursor.moveToFirst();
		String currentUser = getUsername();
		String creator = cursor.getString(0);
		if (currentUser.equalsIgnoreCase(creator)) {
			// delete the event from the database

			int eventsDeleted = db.delete(DbHelper.TABLE_EVENTS,
					DbHelper.EVENT_ID + "=?",
					new String[] { Integer.toString(id) });
			db.close();

			// delete the event from the spreadsheet
			if (eventsDeleted > 0) {
				Log.d(TAG, "Event deleted from the database");
				new DeleteEventOnSpreadsheet().execute(arrayId);
				Toast.makeText(this, "Event deleted.", Toast.LENGTH_LONG)
						.show();
				return true;
			}

			Log.w(TAG, "Problems deleting the event in the database");
			Toast.makeText(this,
					"Problems deleting the event in the database.",
					Toast.LENGTH_LONG).show();
			return false;
		}
		Toast.makeText(
				this,
				"You are not the creator of the event.", Toast.LENGTH_LONG).show();
		Log.d(TAG,
				"Deletion not allowed. " + currentUser + "!= "
						+ creator);
		return false;
	}

	/**
	 * get the current worksheet from the spreadsheet
	 * 
	 * @return current worksheet
	 */
	private WorkSheet getWorkSheet() {
		if (getSpreadsheetFactory() == null) {
			Log.w(TAG, "No account set");
			return null;
		}

		String spreadsheetTitle = prefs.getString(
				(String) getText(R.string.preferencesKeySpreadsheetTitle),
				"event_manager");
		Log.v(TAG, "Spreadsheet title: " + spreadsheetTitle);
		ArrayList<SpreadSheet> spreadsheets = getSpreadsheetFactory()
				.getSpreadSheet(spreadsheetTitle, false);

		if ((spreadsheets == null) || (spreadsheets.size() == 0)) {
			Log.d(TAG, "No spreadsheet found.");
			createWebSpreadSheet(spreadsheetTitle);
			Log.d(TAG, "New Spreadsheet and Worksheet created and initialized.");
		}
		spreadSheetKey = spreadsheets.get(0).getKey();

		// Get the lastSavedEventId from database if it is the first loop of the
		// parsing
		if (lastSavedEventId == -1)
			lastSavedEventId = getMaxEventId(null);

		Log.d(TAG, "Spreadsheets found.");
		return spreadsheets.get(0).getAllWorkSheets().get(0);
	}

	/**
	 * This class create an async task in order to delete an event on the
	 * spreadsheet online
	 * 
	 */
	class DeleteEventOnSpreadsheet extends AsyncTask<String, Boolean, String> {

		@Override
		protected String doInBackground(String... params) {
			WorkSheet ws = getWorkSheet();
			ArrayList<WorkSheetRow> rows = ws.getData(false);
			WorkSheetRow wsr = null;
			for (int i = 0; i < rows.size(); i++) {
				ArrayList<WorkSheetCell> wsc = rows.get(i).getCells();
				// Put in the database only the new events
				if (wsc.get(0).getValue().equalsIgnoreCase(params[0])) {
					wsr = rows.get(i);
					break;
				}
			}
			if (wsr == null) {
				Log.w(TAG, "Problems with get the event from the spreadsheet");
			}
			ws.deleteListRow(spreadSheetKey, wsr);
			Log.d(TAG, "Event deleted from the spreadsheet");
			return "";
		}
	}
}
