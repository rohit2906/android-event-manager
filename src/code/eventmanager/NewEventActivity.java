package code.eventmanager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
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

	private static final String TAG = NewEventActivity.class.getSimpleName();

	public static final int RESULT_EVENT_CREATED = 10;
	public static final int RESULT_EVENT_ERROR = 11;

	private static final int STARTING_DATE_DIALOG_ID = 0;
	private static final int STARTING_TIME_DIALOG_ID = 1;
	private static final int ENDING_DATE_DIALOG_ID = 2;
	private static final int ENDING_TIME_DIALOG_ID = 3;

	EventManagerApp app;
	public DbHelper dbHelper;

	LocationManager locationManager;
	Geocoder geocoder;
	Location location;
	String locationProvider;

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

		// Setup location information
		locationProvider = app.getLocationProvider();
		if (!locationProvider.equalsIgnoreCase(EventManagerApp.LOCATION_PROVIDER_NONE)) {
			// Get the Location Manager Service 
			locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
			geocoder = new Geocoder(this);

			if (locationManager != null) {
				// Get the Last Known Location
				location = locationManager.getLastKnownLocation(locationProvider);

				Log.d(TAG, "Location: " + String.format(
						"Lat:\t %f\nLong:\t %f\nAlt:\t %f",
						location.getLatitude(), location.getLongitude(), location.getAltitude()));

				// Perform geocoding for this location
				try {
					Address address = geocoder.getFromLocation(
							location.getLatitude(), location.getLongitude(), 1).get(0);

					String addressString = "";
					for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
						addressString += (addressString.isEmpty() ? "" : ", ") + address.getAddressLine(i);
					}

					etAddress.setText(addressString);
				} catch (IOException e) {
					Log.w(TAG, "Couldn't get Geocoder data");
				}
			}
		}
	}

	/**
	 * Write the dates on the buttons
	 * 
	 * @param date the date to write
	 * @param button the button on which write the date
	 */
	private void updateDate(Date date, Button button) {
		Log.v(TAG, "Update date on the button");
		button.setText(app.getDateFormatted(date));
	}

	/**
	 * Write the times on the buttons
	 * 
	 * @param time the time to write
	 * @param button the button on which write the time
	 */
	private void updateTime(Date time, Button button) {
		Log.v(TAG, "Update time on the button");
		button.setText(app.getTimeFormatted(time));
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

	/**
	 * Listener of the starting date dialog for setting the starting date
	 */
	private DatePickerDialog.OnDateSetListener startingDateSetListener = new DatePickerDialog.OnDateSetListener() {
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			Log.v(TAG, "Setting starting date");
			starting.setYear(year - 1900);
			starting.setMonth(monthOfYear);
			starting.setDate(dayOfMonth);
			updateDate(starting, btnStartingDate);

			// Update the ending time if the starting time is after the ending
			if (starting.after(ending)) {
				ending = (Date) starting.clone();
				updateDate(ending, btnEndingDate);
			}
		}
	};

	/**
	 * Listener of the starting time dialog for setting the starting time
	 */
	private TimePickerDialog.OnTimeSetListener startingTimeSetListener = new TimePickerDialog.OnTimeSetListener() {

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			Log.v(TAG, "Setting starting time");
			starting.setHours(hourOfDay);
			starting.setMinutes(minute);
			updateTime(starting, btnStartingTime);

			// Update the ending time if the starting time is after the ending
			if (starting.after(ending)) {
				ending = (Date) starting.clone();
				updateTime(ending, btnEndingTime);
			}
		}
	};

	/**
	 * Listener of the ending date dialog for setting the ending date
	 */
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

	/**
	 * Listener of the ending time dialog for setting the ending time
	 */
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
			String dataName = etTitle.getText().toString();
			String dataAddress = etAddress.getText().toString();
			String dataDescription = etDescription.getText().toString();
			String dataCreator = app.getUsername();
			long dataStartingTS = starting.getTime();
			long dataEndingTS = ending.getTime();

			// check the data validity
			if (dataName.isEmpty()) {
				Toast.makeText(this, "Insert the Title", Toast.LENGTH_SHORT).show();
			}else if (starting.before(new Date())) {
				Toast.makeText(this, "The Starting Time is already passed", Toast.LENGTH_LONG).show();
			} else if (ending.before(starting)) {
				Toast.makeText(this, "The Ending Date is before the Starting Date", Toast.LENGTH_LONG).show();
			} else if (dataAddress.isEmpty()) {
				Toast.makeText(this, "Insert the Address", Toast.LENGTH_SHORT).show();
			} else if (dataDescription.isEmpty()) {
				Toast.makeText(this, "Insert the Description", Toast.LENGTH_SHORT).show();
			} else {			
				// open the database
				SQLiteDatabase db = app.getDbHelper().getWritableDatabase();
				ContentValues record = new ContentValues();

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
					
					// Get the Id of the event that we have just created
					int max = app.getMaxDbEventId(db);
					
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
						
						// Update the last Saved Id to prevent notification
						// of this event on your own mobile phone
						app.setLastSavedEventsId(max);

						//set the result for creating the toast when the activity finish
						Log.d(TAG, String.format(
								"Event Created:ID=?\n?Name=?\nAddress=?\nDescription=?\nCreator=?\nStarting=?\nEnding=?\n",
								(Object[]) entry));
					} else {
						recordInserted = false;
					}
				}

				// Close the database
				db.close();

				if (recordInserted) {
					// Set the return code of the activity
					setResult(RESULT_EVENT_CREATED);
					
					// Update the widget
					sendBroadcast(new Intent(EventManagerWidget.REFRESH_WIDGET));
					
					// Close the activity
					finish();
				} else {
					//set the result for creating the toast when the activity finish
					setResult(RESULT_EVENT_ERROR);
					
					// Problem saving record
					Log.w(TAG, "Problem saving record.");
					Toast.makeText(NewEventActivity.this, "Problem creating the event. Retry", Toast.LENGTH_LONG);
				}
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
