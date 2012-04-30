package code.eventmanager;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class NewEventActivity extends Activity implements OnClickListener {

	
	EditText titleNewEvent;
	EditText descriptionNewEvent;
	EditText start_dateNewEvent;
	EditText starting_hourNewEvent;
	EditText ending_dateNewEvent;
	EditText ending_hourNewEvent;
	Button create_buttonNewEvent;
	
	public DbHelper dbHelper;
	
	@SuppressWarnings("unused")
	private static final String TAG = NewEventActivity.class.getSimpleName();

	EventManagerApp app;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		Log.d(TAG, "on create");
		setContentView(R.layout.newevent_layout);
		app = (EventManagerApp) getApplication();
		
		titleNewEvent = (EditText) findViewById(R.id.neweventEditTextTitle);
		descriptionNewEvent = (EditText) findViewById(R.id.newEventEditTextDescription);
		start_dateNewEvent = (EditText) findViewById(R.id.neweventEditTextStartingDate);
		starting_hourNewEvent = (EditText) findViewById(R.id.neweventEditTextStartingHour);
		ending_dateNewEvent = (EditText) findViewById(R.id.neweventEditTextEndingDate);
		ending_hourNewEvent = (EditText) findViewById(R.id.neweventEditTextEndingHour);
		create_buttonNewEvent = (Button) findViewById(R.id.neweventCreateButton);
		
		create_buttonNewEvent.setOnClickListener(this);
		dbHelper = new DbHelper(this);
		
	}
	
	public long getTimeStamp(String myString)
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
		try
		{
			Date parsedDate = dateFormat.parse(myString);
			Timestamp timestamp = new Timestamp(parsedDate.getTime());
			long time = timestamp.getTime();
			return time;
		}catch (Exception e) {
			Log.d("TAG", "Non valable date format");
			return -1;
		}
		
	}

	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
		SQLiteDatabase db = dbHelper.getWritableDatabase(); //open the database
		ContentValues record = new ContentValues();
		
		record.put(DbHelper.EVENTS_NAME, titleNewEvent.getText().toString());
		record.put(DbHelper.EVENTS_DESCRIPTION, descriptionNewEvent.getText().toString());
		//record.put(DbHelper.EVENTS_CREATOR, app.getSpreadsheetFactory().);
		record.put(DbHelper.EVENTS_STARTING_TS, getTimeStamp(starting_hourNewEvent.getText().toString()));
		record.put(DbHelper.EVENTS_ENDING_TS, getTimeStamp(ending_hourNewEvent.getText().toString()));
	}
	
	
}
