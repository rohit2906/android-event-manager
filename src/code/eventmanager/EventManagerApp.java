package code.eventmanager;

import java.util.ArrayList;

import code.eventmanager.auth.AndroidAuthenticator;

import com.pras.SpreadSheet;
import com.pras.SpreadSheetFactory;
import com.pras.WorkSheet;
import com.pras.WorkSheetCell;
import com.pras.WorkSheetRow;

import android.app.Application;
import android.content.ContentValues;
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
	DbHelper dbHelper;

	/**
	 * Reference to preferences and registration to their changes
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
		this.prefs.registerOnSharedPreferenceChangeListener(this);
		setAccount();
		dbHelper = new DbHelper(this);
		Log.i(TAG, "onCreated");
		startActivity(new Intent(this, LoginActivity.class));
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
		if (key == getText(R.string.credentialsKeyDefaultAccount)) {
			spreadsheetFactory = null;
			setAccount();
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
	private void setAccount() {
		if (prefs.getBoolean(
				(String) getText(R.string.credentialsKeyDefaultAccount), true)) {
			Log.v(TAG, "Logging in with default account.");
			spreadsheetFactory = SpreadSheetFactory
					.getInstance(new AndroidAuthenticator(
							getApplicationContext()));
		} else {
			Log.v(TAG, "Logging in with custom account.");
			String email = prefs.getString(
					(String) getText(R.string.credentialsKeyCustomAccountMail),
					"");
			String password = prefs
					.getString(
							(String) getText(R.string.credentialsKeyCustomAccountPassword),
							"");
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
	public synchronized void parseEvents() {
		String spreadsheetTitle = prefs.getString(
				(String) getText(R.string.credentialsKeySpreadsheetTitle), "");
		ArrayList<SpreadSheet> spreadsheets = getSpreadsheetFactory()
				.getSpreadSheet(spreadsheetTitle, true);
		Log.v(TAG, (spreadsheets == null ? "no spreadsheet found"
				: spreadsheets.size()) + " spreadsheets found");
		if (spreadsheets != null) {
			Log.v(TAG, "Parsing events...");
			WorkSheet ws = spreadsheets.get(0).getAllWorkSheets().get(0);
			ArrayList<WorkSheetRow> rows;
			ArrayList<WorkSheetCell> wc;

			SQLiteDatabase db = dbHelper.getWritableDatabase(); // open the
																// database
			ContentValues record = new ContentValues();

			rows = ws.getData(false); // get the rows
			for (int i = 0; i < ws.getRowCount(); i++) { // iterates over all
															// rows
				wc = rows.get(i).getCells(); // get the cells in that row
				record.clear(); // populate the new database record
				record.put(DbHelper.EVENTS_ID,
						Integer.parseInt(wc.get(0).toString()));
				record.put(DbHelper.EVENTS_NAME, wc.get(1).toString());
				record.put(DbHelper.EVENTS_DESCRIPTION, wc.get(2).toString());
				record.put(DbHelper.EVENTS_CREATOR, wc.get(3).toString());
				record.put(DbHelper.EVENTS_STARTING_TS,
						Integer.parseInt(wc.get(3).toString()));
				record.put(DbHelper.EVENTS_ENDING_TS,
						Integer.parseInt(wc.get(4).toString()));
				try {
					db.insertOrThrow(DbHelper.TABLE_EVENTS, null, record); // insert
																			// the
																			// record
																			// into
																			// the
																			// database
					Log.d(TAG, "Record: " + wc.get(0).toString() + " - "
							+ wc.get(1).toString() + " - "
							+ wc.get(3).toString());
				} catch (SQLException e) {
					Log.d(TAG, "Record " + wc.get(0).toString() + " skipped");

				}
			}
			db.close();
			Log.v(TAG, "Events parsed");
		}
	}
}
