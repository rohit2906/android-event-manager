package code.eventmanager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.pras.SpreadSheet;
import com.pras.WorkSheet;

public class NewEventActivity extends Activity implements OnClickListener {

	private EditText etTitle;
	private EditText etAddress;
	private EditText etDescription;
	private Button btnStartingDate;
	private Button btnStartingTime;
	private Button btnEndingDate;
	private Button btnEndingTime;
	private Button create_buttonNewEvent;
	private Button cancel_buttonNewEvent;

	private Date starting;
	private Date ending;
	private static final int STARTING_DATE_DIALOG_ID = 0;
	private static final int STARTING_TIME_DIALOG_ID = 1;
	private static final int ENDING_DATE_DIALOG_ID = 2;
	private static final int ENDING_TIME_DIALOG_ID = 3;

	public DbHelper dbHelper;

	private static final String TAG = NewEventActivity.class.getSimpleName();

	EventManagerApp app;

	/**
	 * Reference to widgets and registration to onClick listener. Create the
	 * starting and ending time set for now. Update the buttons with the current
	 * time.
	 */

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
		setContentView(R.layout.newevent_layout);
		app = (EventManagerApp) getApplication();

		etTitle = (EditText) findViewById(R.id.neweventEditTextTitle);
		etAddress = (EditText) findViewById(R.id.neweventEditTextAddress);
		etDescription = (EditText) findViewById(R.id.neweventEditTextDescription);
		btnStartingDate = (Button) findViewById(R.id.neweventButtonStartingDate);
		btnStartingTime = (Button) findViewById(R.id.neweventButtonStartingTime);
		btnEndingDate = (Button) findViewById(R.id.neweventButtonEndingDate);
		btnEndingTime = (Button) findViewById(R.id.neweventButtonEndingTime);
		create_buttonNewEvent = (Button) findViewById(R.id.neweventButtonCreate);
		cancel_buttonNewEvent = (Button) findViewById(R.id.neweventButtonCancel);

		btnStartingDate.setOnClickListener(this);
		btnStartingTime.setOnClickListener(this);
		btnEndingDate.setOnClickListener(this);
		btnEndingTime.setOnClickListener(this);
		create_buttonNewEvent.setOnClickListener(this);
		cancel_buttonNewEvent.setOnClickListener(this);

		starting = new Date();
		ending = new Date();

		// display the current date/time
		updateDate(starting, btnStartingDate);
		updateTime(starting, btnStartingTime);
		updateDate(ending, btnEndingDate);
		updateTime(ending, btnEndingTime);
	}

	/**
	 * Write the dates on the buttons
	 * 
	 * @param date
	 *            the date to write
	 * @param button
	 *            the button on which write the date
	 */
	private void updateDate(Date date, Button button) {
		button.setText(date.getDate() + "-" + (date.getMonth() + 1) + "-"
				+ (date.getYear() + 1900));
	}

	/**
	 * Write the times on the buttons
	 * 
	 * @param time
	 *            the time to write
	 * @param button
	 *            the button on which write the time
	 */
	private void updateTime(Date time, Button button) {
		button.setText(time.getHours() + ":" + time.getMinutes());
	}

	/**
	 * Select which dialog to show based on the the parameter id of the
	 * showDialog() into the onClick method
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		Log.i(TAG, "onCreateDialog");
		Log.d(TAG, "Dialog id: " + id);
		switch (id) {
		case STARTING_DATE_DIALOG_ID:
			return new DatePickerDialog(this, startingDateSetListener,
					starting.getYear() + 1900, starting.getMonth(),
					starting.getDate());
		case STARTING_TIME_DIALOG_ID:
			return new TimePickerDialog(this, startingTimeSetListener,
					starting.getHours(), starting.getMinutes(), true);
		case ENDING_DATE_DIALOG_ID:
			return new DatePickerDialog(this, endingDateSetListener,
					ending.getYear() + 1900, ending.getMonth(),
					ending.getDate());
		case ENDING_TIME_DIALOG_ID:
			return new TimePickerDialog(this, endingTimeSetListener,
					starting.getHours(), starting.getMinutes(), true);
		}
		return null;
	}

	// Listener of the starting date dialog for setting the starting date
	private DatePickerDialog.OnDateSetListener startingDateSetListener = new DatePickerDialog.OnDateSetListener() {
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			starting.setYear(year - 1900);
			starting.setMonth(monthOfYear);
			starting.setDate(dayOfMonth);
			updateDate(starting, btnStartingDate);
		}
	};

	// Listener of the starting time dialog for setting the starting time
	private TimePickerDialog.OnTimeSetListener startingTimeSetListener = new TimePickerDialog.OnTimeSetListener() {

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			starting.setHours(hourOfDay);
			starting.setMinutes(minute);
			updateTime(starting, btnStartingTime);
		}
	};

	// Listener of the ending date dialog for setting the ending date
	private DatePickerDialog.OnDateSetListener endingDateSetListener = new DatePickerDialog.OnDateSetListener() {
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			ending.setYear(year - 1900);
			ending.setMonth(monthOfYear);
			ending.setDate(dayOfMonth);
			updateDate(ending, btnEndingDate);
		}
	};

	// Listener of the ending time dialog for setting the ending time
	private TimePickerDialog.OnTimeSetListener endingTimeSetListener = new TimePickerDialog.OnTimeSetListener() {

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			ending.setHours(hourOfDay);
			ending.setMinutes(minute);
			updateTime(ending, btnEndingTime);
		}
	};

	/**
	 * Convert a date into a timestamp
	 * 
	 * @param year
	 * @param month
	 * @param day
	 * @param hour
	 * @param minute
	 * @return milliseconds
	 */
	private long date2Timestamp(int year, int month, int day, int hour,
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

	private String getCreator() {
		String creator;
		boolean checked = app.getPrefs()
				.getBoolean(
						getText(R.string.credentialsKeyDefaultAccount)
								.toString(), true);

		if (checked) {
			AccountManager manager = AccountManager
					.get(getApplicationContext());
			creator = manager.getAccountsByType("com.google")[0].name;
		} else {
			creator = app.getPrefs().getString(
					getText(R.string.credentialsKeyCustomAccountMail)
							.toString(), "");
		}

		return creator;
	}

	/**
	 * catch the click of the dates, times, create buttons
	 */
	@Override
	public void onClick(View v) {
		Log.i(TAG, "onClick");
		switch (v.getId()) {
		case R.id.neweventButtonStartingDate:
			showDialog(STARTING_DATE_DIALOG_ID);
			break;
		case R.id.neweventButtonStartingTime:
			showDialog(STARTING_TIME_DIALOG_ID);
			break;
		case R.id.neweventButtonEndingDate:
			showDialog(ENDING_DATE_DIALOG_ID);
			break;
		case R.id.neweventButtonEndingTime:
			showDialog(ENDING_TIME_DIALOG_ID);
			break;
		case R.id.neweventButtonCreate:

			SQLiteDatabase db = app.getDbHelper().getWritableDatabase(); // open
																			// the
																			// database
			ContentValues record = new ContentValues();

			record.put(DbHelper.EVENTS_NAME, etTitle.getText().toString());

			record.put(DbHelper.EVENTS_ADDRESS, etAddress.getText().toString());

			record.put(DbHelper.EVENTS_DESCRIPTION, etDescription.getText()
					.toString());//

			record.put(DbHelper.EVENTS_CREATOR, getCreator());

			record.put(
					DbHelper.EVENTS_STARTING_TS,
					new Long(date2Timestamp(starting.getYear(),
							starting.getMonth(), starting.getDate(),
							starting.getHours(), starting.getMinutes()))
							.toString());

			record.put(
					DbHelper.EVENTS_ENDING_TS,
					new Long(date2Timestamp(ending.getYear(),
							ending.getMonth(), ending.getDate(),
							ending.getHours(), ending.getMinutes())).toString());

			boolean cnt = true;

			try {
				db.insertOrThrow(DbHelper.TABLE_EVENTS, null, record); // insert
																		// the
																		// record
																		// into
																		// the
																		// database
				Log.v(TAG, "Record inserted");

			} catch (SQLException e) {
				Log.w(TAG, "Record not inserted");
				cnt = false;
			}

			if (cnt) {
				record.clear();

				String[] entry = new String[7];
				String query = "SELECT * FROM " + DbHelper.TABLE_EVENTS
						+ " WHERE " + DbHelper.EVENTS_ID + " IN (SELECT MAX("
						+ DbHelper.EVENTS_ID + ") FROM "
						+ DbHelper.TABLE_EVENTS + ")";
				Cursor cursor = db.rawQuery(query, null);

				if (cursor.moveToFirst()) {
					entry[0] = new Integer(cursor.getInt(0)).toString();
					entry[1] = cursor.getString(1);
					entry[2] = cursor.getString(2);
					entry[3] = cursor.getString(3);
					entry[4] = cursor.getString(4);
					entry[5] = cursor.getString(5);
					entry[6] = new Long(cursor.getLong(6)).toString();

					new UpdateSpreadsheet().execute(entry);
				}

			}

			db.close();
			finish();
			break;
		case R.id.neweventButtonCancel:
			finish();
			break;
		default:
			break;
		}
	}

	class UpdateSpreadsheet extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... params) {

			String spreadsheetTitle = app.getPrefs().getString(
					(String) getText(R.string.credentialsKeySpreadsheetTitle),
					"event_manager");
			ArrayList<SpreadSheet> spreadsheets = app.getSpreadsheetFactory()
					.getSpreadSheet(spreadsheetTitle, false);
			WorkSheet ws = spreadsheets.get(0).getAllWorkSheets().get(0);
			HashMap<String, String> entry = new HashMap<String, String>();
			entry.put(DbHelper.EVENTS_ID, params[0]);
			entry.put(DbHelper.EVENTS_NAME, params[1]);
			entry.put(DbHelper.EVENTS_ADDRESS, params[2]);
			entry.put(DbHelper.EVENTS_DESCRIPTION, params[3]);
			entry.put(DbHelper.EVENTS_CREATOR, params[4]);
			entry.put(DbHelper.EVENTS_STARTING_TS, params[5]);
			entry.put(DbHelper.EVENTS_ENDING_TS, params[6]);
			ws.addRecord(spreadsheets.get(0).getKey(), entry);
			return params[0];
		}

		@Override
		protected void onPostExecute(String result) {
			Toast.makeText(NewEventActivity.this, "spreadsheet updated",
					Toast.LENGTH_LONG).show();
		}

	}
}
