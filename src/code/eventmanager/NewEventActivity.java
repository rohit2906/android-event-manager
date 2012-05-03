package code.eventmanager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
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
		Log.v(TAG, "Update date on the button");
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
		Log.v(TAG, "Update time on the button");
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
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			Log.v(TAG, "Setting starting date");
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
			Log.v(TAG, "Setting starting time");
			starting.setHours(hourOfDay);
			starting.setMinutes(minute);
			updateTime(starting, btnStartingTime);
		}
	};

	// Listener of the ending date dialog for setting the ending date
	private DatePickerDialog.OnDateSetListener endingDateSetListener = new DatePickerDialog.OnDateSetListener() {
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			Log.v(TAG, "Setting ending date");
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
			Log.v(TAG, "Setting ending time");
			ending.setHours(hourOfDay);
			ending.setMinutes(minute);
			updateTime(ending, btnEndingTime);
		}
	};

	



	/**
	 * Catch the click of the dates, times, create buttons
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
			// open the database
			SQLiteDatabase db = app.getDbHelper().getWritableDatabase();
			ContentValues record = new ContentValues();
			
			String dataName = etTitle.getText().toString();
			String dataAddress = etAddress.getText().toString();
			String dataDescription = etDescription.getText().toString();
			String dataCreator = app.getUsername();
			long dataStartingTS = app.date2Timestamp(starting.getYear(), starting.getMonth(), starting.getDate(), starting.getHours(), starting.getMinutes());
			long dataEndingTS = app.date2Timestamp(ending.getYear(), ending.getMonth(), ending.getDate(), ending.getHours(), ending.getMinutes());

			record.put(DbHelper.EVENT_NAME, dataName);
			record.put(DbHelper.EVENT_ADDRESS, dataAddress);
			record.put(DbHelper.EVENT_DESCRIPTION, dataDescription);
			record.put(DbHelper.EVENT_CREATOR, dataCreator);
			record.put(DbHelper.EVENT_STARTING_TS, dataStartingTS);
			record.put(DbHelper.EVENT_ENDING_TS, dataEndingTS);
			boolean recordInserted = true;
			try {
				// insert the record into the database
				db.insertOrThrow(DbHelper.TABLE_EVENTS, null, record); 
				Log.v(TAG, "Record inserted");

			} catch (SQLException e) {
				Log.w(TAG, "Record not inserted");
				recordInserted = false;
			}

			if (recordInserted) {
				record.clear();
				int max = app.getMaxEventId(db);
				if (max != -1) {
					String[] entry = new String[7];
					
					entry[0] = Integer.toString(max);
					entry[1] = dataName;
					entry[2] = dataAddress;
					entry[3] = dataDescription;
					entry[4] = dataCreator;
					entry[5] = Long.toString(dataStartingTS);
					entry[6] = Long.toString(dataEndingTS);
					new UpdateSpreadsheet().execute(entry);
					
					//set the result for create the toast when the activity finish
					setResult(RESULT_OK);
				} else {
					recordInserted = false;
					
					//set the result for create the toast when the activity finish
					setResult(RESULT_CANCELED);
				}
			}

			// Close the database
			db.close();
			
			if (recordInserted) {
				// Close the activity
				finish();
			} else {
				// Problem saving record
				Log.w(TAG, "Problem saving record.");
				Toast.makeText(NewEventActivity.this, "Problem creating the event. Retry", Toast.LENGTH_LONG);
			}
			
			break;
			
		case R.id.neweventButtonCancel:
			// Close the activity
			finish();
			break;
			
		default:
			break;
		}
	}

	/**
	 * This class create an async task in order to update the spreadsheet online
	 *
	 */
	class UpdateSpreadsheet extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... params) {
			String spreadsheetTitle = app.getPrefs().getString(
					(String) getText(R.string.preferencesKeySpreadsheetTitle), "event_manager");
			ArrayList<SpreadSheet> spreadsheets = app.getSpreadsheetFactory()
					.getSpreadSheet(spreadsheetTitle, false);
			
			WorkSheet ws = spreadsheets.get(0).getAllWorkSheets().get(0);
			HashMap<String, String> entry = new HashMap<String, String>();
			
			entry.put(DbHelper.EVENT_ID, params[0]);
			entry.put(DbHelper.EVENT_NAME, params[1]);
			entry.put(DbHelper.EVENT_ADDRESS, params[2]);
			entry.put(DbHelper.EVENT_DESCRIPTION, params[3]);
			entry.put(DbHelper.EVENT_CREATOR, params[4]);
			entry.put(DbHelper.EVENT_STARTING_TS, params[5]);
			entry.put(DbHelper.EVENT_ENDING_TS, params[6]);
			
			ws.addRecord(spreadsheets.get(0).getKey(), entry);
			return params[0];
		}
	}
}
