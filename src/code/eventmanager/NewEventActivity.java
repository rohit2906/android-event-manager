package code.eventmanager;

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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.d(TAG, "on create");
		setContentView(R.layout.newevent_layout);
		
		titleNewEvent = (EditText) findViewById(R.id.neweventEditTextTitle);
		descriptionNewEvent = (EditText) findViewById(R.id.newEventEditTextDescription);
		start_dateNewEvent = (EditText) findViewById(R.id.neweventEditTextStartingDate);
		starting_hourNewEvent = (EditText) findViewById(R.id.neweventEditTextStartingHour);
		ending_dateNewEvent = (EditText) findViewById(R.id.neweventEditTextEndingDate);
		ending_hourNewEvent = (EditText) findViewById(R.id.neweventEditTextEndingHour);
		create_buttonNewEvent = (Button) findViewById(R.id.neweventCreateButton);
		
		dbHelper = new DbHelper(this);
		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
		SQLiteDatabase db = dbHelper.getWritableDatabase(); //open the database
		ContentValues record = new ContentValues();
		
		record.put(DbHelper.EVENTS_NAME, titleNewEvent);
		record.put(DbHelper.EVENTS_DESCRIPTION, descriptionNewEvent);
		record.put(DbHelper.EVENTS_CREATOR, wsc.get(3).getValue());
		record.put(DbHelper.EVENTS_STARTING_TS,
				Integer.parseInt(wsc.get(3).getValue()));
		record.put(DbHelper.EVENTS_ENDING_TS,
				Integer.parseInt(wsc.get(4).getValue()));
	}
	
	
}
