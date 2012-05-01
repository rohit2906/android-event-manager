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
 * Application class in order to share all information that are needed to the
 * other activities
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
		// TODO remove later or put a preference like "run in background"
		alarmManager.cancel(pollerTriggerPendingIntent);
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
			boolean defaultAccount = prefs.getBoolean(
					getText(R.string.credentialsKeyDefaultAccount).toString(),
					false);
			if (defaultAccount == true) {
				setDefaultAccount();
			} else {
				// check if user e-mail and password are emptyString email = prefs.getString(
				String email = prefs.getString(
						(String) getText(R.string.credentialsKeyCustomAccountMail), "");
				String password = prefs.getString(
						(String) getText(R.string.credentialsKeyCustomAccountPassword), "");
				
				if (email.isEmpty() || password.isEmpty()) 
					Log.i(TAG, "No account set");
				else
					setAnotherAccount(email, password);
			}
		}

		// check if the user type a new username or password for the custom
		// account
		else if (key == getText(R.string.credentialsKeyCustomAccountMail)
				|| key == getText(R.string.credentialsKeyCustomAccountPassword))
		{
			String email = prefs.getString(
					(String) getText(R.string.credentialsKeyCustomAccountMail), "");
			String password = prefs.getString(
					(String) getText(R.string.credentialsKeyCustomAccountPassword), "");
			
			if (email != "" && password != "")
				setAnotherAccount(email, password);
		}
		
		else if (key == getText(R.string.credentialsKeyMinutesBetweenUpdates)) {
			Log.d(TAG, "Change update timer");
			setAlarm4Poller();
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
	 * Allocate a new spreadsheetfactory for default account on the smartphone
	 */
	public void setDefaultAccount() {
		Log.d(TAG, "Logging in with default account.");
		spreadsheetFactory = SpreadSheetFactory
				.getInstance(new AndroidAuthenticator(getApplicationContext()));
	}

	/**
	 * Allocate a new spreadsheetfactory for the new custom account
	 * 
	 * @param email username
	 * @param password password
	 */
	public void setAnotherAccount(String email, String password) {
		Log.d(TAG, "Setting up custom account");
		Log.v(TAG, "Email: " + email);
		spreadsheetFactory = SpreadSheetFactory.getInstance(email, password);
	}

	/**
	 * Parse the events in the spreadsheet
	 * 
	 * @param spreadsheet the spreadsheet with events
	 * @param worksheet the spreadsheet with events
	 */
	public int parseEvents() {
		if (getSpreadsheetFactory() == null) {
			Log.w(TAG, "No account set");
			return -1;
		}
		
		String spreadsheetTitle = prefs.getString((String) getText(R.string.credentialsKeySpreadsheetTitle), "event_manager");
		Log.v(TAG, "Spreadsheet title: " + spreadsheetTitle);
		ArrayList<SpreadSheet> spreadsheets = getSpreadsheetFactory().getSpreadSheet(spreadsheetTitle, false);
		
		if ((spreadsheets == null) || (spreadsheets.size() == 0)) {
			Log.d(TAG, "No spreadsheet found. Creating new one.");
			getSpreadsheetFactory().createSpreadSheet(spreadsheetTitle);
			return 0;
		}
		
		Log.d(TAG, "Spreadsheets found. Parsing events...");
		WorkSheet ws = spreadsheets.get(0).getAllWorkSheets().get(0);
		ArrayList<WorkSheetRow> rows = ws.getData(false);

		SQLiteDatabase db = dbHelper.getWritableDatabase(); // open the database
		ContentValues record = new ContentValues();

		int newEvents = 0;
		Log.v(TAG, "Number of rows: " + rows.size());
		for (int i = 0; i < rows.size(); i++) { // iterates over all rows
			ArrayList<WorkSheetCell> wsc = rows.get(i).getCells(); // get the
																	// cells in
																	// that row
			record.clear(); // populate the new database record
			record.put(DbHelper.EVENTS_ID,
					Integer.parseInt(wsc.get(0).getValue()));
			record.put(DbHelper.EVENTS_NAME, wsc.get(1).getValue());
			record.put(DbHelper.EVENTS_ADDRESS, wsc.get(2).getValue());
			record.put(DbHelper.EVENTS_DESCRIPTION, wsc.get(3).getValue());
			record.put(DbHelper.EVENTS_CREATOR, wsc.get(4).getValue());
			record.put(DbHelper.EVENTS_STARTING_TS,
					Integer.parseInt(wsc.get(5).getValue()));
			record.put(DbHelper.EVENTS_ENDING_TS,
					Integer.parseInt(wsc.get(6).getValue()));
			newEvents++;
			try {
				db.insertOrThrow(DbHelper.TABLE_EVENTS, null, record); // insert
																		// the
																		// record
																		// into
																		// the
																		// database
				Log.v(TAG, "Record: " + wsc.get(0).toString() + " - "
						+ wsc.get(1).toString() + " - " + wsc.get(4).toString());
			} catch (SQLException e) {
				Log.w(TAG, "Record " + wsc.get(0).toString() + " skipped");

			}
		}
		db.close();
		Log.d(TAG, "Events parsed");
		return newEvents;
	}

	public void setAlarm4Poller() {
		Log.d(TAG, "Updating poller alarm");
		alarmManager.cancel(pollerTriggerPendingIntent);
		int minutes = Integer.parseInt(prefs.getString(getText(R.string.credentialsKeyMinutesBetweenUpdates).toString().trim(), "1"));
		if (minutes > 0) {
			// set the alarm (it could be approximately). RTC means that the alarm'll not wake up the device if it's sleeping. The next
			// parameter specifies the time is in milliseconds.
			alarmManager.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(), minutes * 60000, pollerTriggerPendingIntent);
			Log.v(TAG, "Alarm set every " + minutes + " minutes");
		} else {
			Log.v(TAG, "Automatic updates disabled");
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
}
