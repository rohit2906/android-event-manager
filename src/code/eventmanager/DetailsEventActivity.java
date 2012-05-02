package code.eventmanager;


import android.os.Bundle;

import java.util.ArrayList;
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
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.pras.SpreadSheet;
import com.pras.WorkSheet;
import com.pras.WorkSheetRow;

public class DetailsEventActivity extends Activity implements OnClickListener{

	private TextView tvTitle;
	private TextView tvStarting;
	private TextView tvEnding;
	private TextView tvAddress;
	private TextView tvDescription;
	
	private int event_id = getIntent().getIntExtra(EventsActivity.EVENT_DETAILS_ID, -1);
	private String event_name = getIntent().getStringExtra(EventsActivity.EVENT_DETAILS_NAME);
	private String event_address = getIntent().getStringExtra(EventsActivity.EVENT_DETAILS_ADDRESS);
	private String event_description = getIntent().getStringExtra(EventsActivity.EVENT_DETAILS_DESCRIPTION);
	private String event_creator = getIntent().getStringExtra(EventsActivity.EVENT_DETAILS_CREATOR);
	private String event_starting = getIntent().getStringExtra(EventsActivity.EVENT_DETAILS_STARTING);
	private String event_ending = getIntent().getStringExtra(EventsActivity.EVENT_DETAILS_ENDING);
	
	private Button btnReturn;
	private Button btnDelete;
	

	public DbHelper dbHelper;
	
	
	private static final String TAG = NewEventActivity.class.getSimpleName();

	EventManagerApp app;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		Log.d(TAG, "onCreate");
		setContentView(R.layout.detailsevent_layout);
		app = (EventManagerApp) getApplication();

		super.onCreate(savedInstanceState);
		
		tvTitle = (TextView) findViewById(R.id.detailseventTextViewTitle);
		tvTitle.setText(event_name);
		
		tvStarting = (TextView) findViewById(R.id.detailseventTextViewStarting);
		tvStarting.setText(event_starting);
		
		tvEnding = (TextView) findViewById(R.id.detailseventTextViewEnding);
		tvEnding.setText(event_ending);
		
		tvAddress = (TextView) findViewById(R.id.detailseventTextViewAddress);
		tvAddress.setText(event_ending);
		
		tvDescription = (TextView) findViewById(R.id.detailseventTextViewDescription);
		tvDescription.setText(event_description);
		
		btnReturn = (Button) findViewById(R.id.detailseventButtonReturn);
		btnReturn.setOnClickListener(this);
		
		//if the creator of the event is also the user, we'll implement the button
		if(app.getCreator() == event_creator)
		{
			btnDelete = (Button) findViewById(R.id.detailseventButtonDelete);
			btnDelete.setOnClickListener(this);
		}
		
		
	}
	
	
	@Override
	public void onClick(View v) {
		Log.i(TAG, "onClick");

		switch(v.getId()){
		
		case R.id.detailseventButtonReturn:
			Log.d(TAG, "finished");
			finish();
			break;

		case R.id.detailseventButtonDelete:
			
			if(this.event_id != -1)
			{
				Log.d(TAG, "event deleted");
				
				//delete the event from the database
				SQLiteDatabase db = app.getDbHelper().getWritableDatabase();
				db.delete(DbHelper.TABLE_EVENTS, DbHelper.EVENT_ID+"=?", new String[] {Integer.toString(this.event_id)});
				db.close();
				
				//delete the event from the spreadsheet :
				String spreadsheetTitle = app.getPrefs().getString(
						EventsActivity.EVENT_DETAILS_NAME, "event_manager");
				ArrayList<SpreadSheet> spreadsheets = app.getSpreadsheetFactory()
						.getSpreadSheet(spreadsheetTitle, false);
				WorkSheet ws = spreadsheets.get(0).getAllWorkSheets().get(0);
				
				
			}
			finish();	
			break;

		default:
			finish();
			break;

		}
	}
	
	

}
