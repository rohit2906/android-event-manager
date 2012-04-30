package code.eventmanager;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

public class NewEventActivity extends Activity implements OnClickListener {

	private EditText titleNewEvent;
	private EditText descriptionNewEvent;
	private Button btnStartingDate;
	private Button btnStartingTime;
	private Button btnEndingDate;
	private Button btnEndingTime;
	private Date starting;
	private Date ending;

	static final int STARTING_DATE_DIALOG_ID = 0;
	static final int STARTING_TIME_DIALOG_ID = 1;
	static final int ENDING_DATE_DIALOG_ID = 2;
	static final int ENDING_TIME_DIALOG_ID = 3;

	Button create_buttonNewEvent;

	public DbHelper dbHelper;

	private static final String TAG = NewEventActivity.class.getSimpleName();

	EventManagerApp app;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		Log.d(TAG, "onCreate");
		setContentView(R.layout.newevent_layout);
		app = (EventManagerApp) getApplication();

		titleNewEvent = (EditText) findViewById(R.id.neweventEditTextTitle);
		descriptionNewEvent = (EditText) findViewById(R.id.neweventEditTextDescription);
		btnStartingDate = (Button) findViewById(R.id.neweventButtonStartingDate);
		btnStartingTime = (Button) findViewById(R.id.neweventButtonStartingTime);
		btnEndingDate = (Button) findViewById(R.id.neweventButtonEndingDate);
		btnEndingTime = (Button) findViewById(R.id.neweventButtonEndingTime);
		create_buttonNewEvent = (Button) findViewById(R.id.neweventCreateButton);

		btnStartingDate.setOnClickListener(this);
		btnStartingTime.setOnClickListener(this);
		btnEndingDate.setOnClickListener(this);
		btnEndingTime.setOnClickListener(this);

		starting = new Date();
		ending = new Date();

		// display the current date/time
		updateDate(starting, btnStartingDate);
		updateTime(starting, btnStartingTime);
		updateDate(ending, btnEndingDate);
		updateTime(ending, btnEndingTime);

		create_buttonNewEvent.setOnClickListener(this);
		dbHelper = new DbHelper(this);

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
	 * Select which dialog to show based on the the parameter id of the showDialog() into the onClick method
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
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

	public long getTimeStamp(String myString) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
		try {
			Date parsedDate = dateFormat.parse(myString);
			Timestamp timestamp = new Timestamp(parsedDate.getTime());
			long time = timestamp.getTime();
			return time;
		} catch (Exception e) {
			Log.d("TAG", "Non valable date format");
			return -1;
		}

	}

	/**
	 * catch the click of the dates, times, create buttons
	 */
	@Override
	public void onClick(View v) {

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
		case R.id.neweventCreateButton:
			/*record.put(DbHelper.EVENTS_NAME, titleNewEvent.getText().toString());
			 record.put(DbHelper.EVENTS_DESCRIPTION, descriptionNewEvent.getText().toString()); // record.put(DbHelper.EVENTS_CREATOR,
			 app.getSpreadsheetFactory().);
			 record.put(DbHelper.EVENTS_STARTING_TS,
			 getTimeStamp(starting_hourNewEvent.getText().toString()));
			 record.put(DbHelper.EVENTS_ENDING_TS,
			 getTimeStamp(ending_hourNewEvent .getText().toString()));*/
			break;
		default:
			break;
		}

		/*
		 * 
		 */
	}
}
