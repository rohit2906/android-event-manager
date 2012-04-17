package code.eventmanager;

import java.util.ArrayList;
import code.eventmanager.auth.AndroidAuthenticator;
import com.pras.SpreadSheet;
import com.pras.SpreadSheetFactory;
import com.pras.WorkSheet;
import com.pras.WorkSheetCell;
import com.pras.WorkSheetRow;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * 
 * @author Daniele Vitali
 * 
 *         Application class in order to share all information that are needed
 *         to the other activities
 */
public class EventManagerApp extends Application implements
		OnSharedPreferenceChangeListener {

	private static final String TAG = EventManagerApp.class.getSimpleName();

	private SharedPreferences prefs;
	private SpreadSheetFactory spreadsheetFactory;
	private AlarmManager alarmManager;
	private PendingIntent pollerTriggerPendingIntent;

	public DbHelper dbHelper;

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

		Intent intent = new Intent(this, PollerService.class);

		// In order to set the alarm we need a pending intent.
		pollerTriggerPendingIntent = PendingIntent.getService(
				getApplicationContext(), -1, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		// Get the alarm service from the system
		alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		Log.i(TAG, "onTerminated");
	}

	/**
	 * When the default account checkbox in the preferences change, it will
	 * reset the account
	 */
	public synchronized void onSharedPreferenceChanged(
			SharedPreferences sharedPreferences, String key) {
		Log.i(TAG, "onSharedPreferenceChanged");
		if (key == getText(R.string.credentialsKeyDefaultAccount)) {
			Log.d(TAG, "Change default account checkbox");
			spreadsheetFactory = null;
			setAccount();
		} else if (key == getText(R.string.credentialsKeyMinutesBetweenUpdates)) {
			Log.d(TAG, "Change update timer");
			int interval = prefs
					.getInt((String) getText(R.string.preferencesMinutesBetweenUpdatesText),
							60)*1000;
			setAlarm4Poller(interval);
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
	 * Check whether to use the default account or a custom account to connect
	 * to the spreadsheet
	 */
	public void setAccount() {
		Log.d(TAG, "Setting up account");
		if (prefs.getBoolean(
				(String) getText(R.string.credentialsKeyDefaultAccount), true)) {
			Log.d(TAG, "Logging in with default account.");
			spreadsheetFactory = SpreadSheetFactory
					.getInstance(new AndroidAuthenticator(
							getApplicationContext()));
		} else {
			Log.d(TAG, "Logging in with custom account.");
			String email = prefs.getString(
					(String) getText(R.string.credentialsKeyCustomAccountMail),
					"");
			String password = prefs
					.getString(
							(String) getText(R.string.credentialsKeyCustomAccountPassword),
							"");
			Log.v(TAG, "Email: " + email + " - Password: " + password);
			spreadsheetFactory = SpreadSheetFactory
					.getInstance(email, password);
		}
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
		String spreadsheetTitle = prefs.getString(
				(String) getText(R.string.credentialsKeySpreadsheetTitle), "");
		Log.v(TAG, "Spreadsheet title: " + spreadsheetTitle);
		ArrayList<SpreadSheet> spreadsheets = getSpreadsheetFactory()
				.getSpreadSheet(spreadsheetTitle, true);
		if (spreadsheets == null) {
			Log.d(TAG, "No spreadsheet found. Creating new one.");
			getSpreadsheetFactory().createSpreadSheet(spreadsheetTitle);
			return -1;
		}
		Log.d(TAG, spreadsheets.size() + " spreadsheets found");
		Log.d(TAG, "Parsing events...");
		WorkSheet ws = spreadsheets.get(0).getAllWorkSheets().get(0);
		ArrayList<WorkSheetRow> rows = ws.getData(false);

		SQLiteDatabase db = dbHelper.getWritableDatabase(); // open the
															// database
		ContentValues record = new ContentValues();

		int newEvents = 0;
		Log.v(TAG, "Number of rows: " + rows.size());
		for (int i = 0; i < rows.size(); i++) { // iterates over all rows
			ArrayList<WorkSheetCell> wsc=rows.get(i).getCells();	// get the cells in that row
			record.clear(); // populate the new database record
			record.put(DbHelper.EVENTS_ID,
					Integer.parseInt(wsc.get(0).getValue()));
			record.put(DbHelper.EVENTS_NAME, wsc.get(1).getValue());
			record.put(DbHelper.EVENTS_DESCRIPTION, wsc.get(2).getValue());
			record.put(DbHelper.EVENTS_CREATOR, wsc.get(3).getValue());
			record.put(DbHelper.EVENTS_STARTING_TS,
					Integer.parseInt(wsc.get(3).getValue()));
			record.put(DbHelper.EVENTS_ENDING_TS,
					Integer.parseInt(wsc.get(4).getValue()));
			newEvents++;
			try {
				db.insertOrThrow(DbHelper.TABLE_EVENTS, null, record); // insert
																		// the
																		// record
																		// into
																		// the
																		// database
				Log.v(TAG, "Record: " + wsc.get(0).toString() + " - "
						+ wsc.get(1).toString() + " - " + wsc.get(3).toString());
			} catch (SQLException e) {
				Log.w(TAG, "Record " + wsc.get(0).toString() + " skipped");

			}
		}
		db.close();
		Log.d(TAG, "Events parsed");
		return newEvents;
	}

	public void setAlarm4Poller(int interval) {
		Log.d(TAG, "Updating poller alarm");
		Log.d(TAG, "Alarm set every " + interval + " milliseconds");
		if (interval > 0)
			// set the alarm (it could be approximately). RTC means that the
			// alarm'll not wake up the device if it's sleeping. The next
			// parameter specifies the time is in milliseconds.
			alarmManager.setInexactRepeating(AlarmManager.RTC,
					System.currentTimeMillis(), interval,
					pollerTriggerPendingIntent);
		else
			alarmManager.cancel(pollerTriggerPendingIntent);
	}

	/**
	 * Get the reference to the dbHelper of the application
	 * 
	 * @return reference to DbHelper
	 */
	public DbHelper getDbHelper() {
		return dbHelper;
	}
}
