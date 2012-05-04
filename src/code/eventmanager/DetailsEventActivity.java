package code.eventmanager;

import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class DetailsEventActivity extends Activity implements OnClickListener {

	private static final String TAG = NewEventActivity.class.getSimpleName();

	public static final String EVENT_DETAILS_ID = "EVENT_DETAILS_ID";
	public static final int RESULT_EVENT_DELETED = 10;

	EventManagerApp app;

	private TextView tvTitle;
	private TextView tvStarting;
	private TextView tvEnding;
	private TextView tvAddress;
	private TextView tvDescription;

	private Button btnReturn;
	private Button btnDelete;

	private long eventId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.detailsevent_layout);

		app = (EventManagerApp) getApplication();

		Intent callingIntent = this.getIntent();
		eventId = callingIntent.getLongExtra(EVENT_DETAILS_ID, -1);

		// event no more available in the database for some reason
		if (eventId < 0) {
			setResult(RESULT_EVENT_DELETED);
			finish();
		} else {
			// Get the data from the database
			SQLiteDatabase db = app.getDbHelper().getReadableDatabase();
			Cursor cursor = db.query(DbHelper.TABLE_EVENTS, null,
					DbHelper.EVENT_ID + "=?", new String[] { Long.toString(eventId) },
					null, null, DbHelper.EVENT_STARTING_TS + " DESC", "1");
			cursor.moveToFirst();

			// Write the data in the layout
			tvTitle = (TextView) findViewById(R.id.detailseventTextViewTitle);
			tvTitle.setText(cursor.getString(cursor.getColumnIndex(DbHelper.EVENT_NAME)));

			Date startingDate = new Date(cursor.getLong(cursor.getColumnIndex(DbHelper.EVENT_STARTING_TS)));
			tvStarting = (TextView) findViewById(R.id.detailseventTextViewStarting);
			tvStarting.setText(app.getDateTimeFormatted(startingDate));

			Date endingDate = new Date(cursor.getLong(cursor.getColumnIndex(DbHelper.EVENT_ENDING_TS)));
			tvEnding = (TextView) findViewById(R.id.detailseventTextViewEnding);
			tvEnding.setText(app.getDateTimeFormatted(endingDate));

			tvAddress = (TextView) findViewById(R.id.detailseventTextViewAddress);
			tvAddress.setText(cursor.getString(cursor.getColumnIndex(DbHelper.EVENT_ADDRESS)));

			tvDescription = (TextView) findViewById(R.id.detailseventTextViewDescription);
			tvDescription.setText(cursor.getString(cursor.getColumnIndex(DbHelper.EVENT_DESCRIPTION)));

			// Set buttons' listeners
			btnReturn = (Button) findViewById(R.id.detailseventButtonReturn);
			btnReturn.setOnClickListener(this);

			btnDelete = (Button) findViewById(R.id.detailseventButtonDelete);
			btnDelete.setOnClickListener(this);

			// Close the cursor and the database
			cursor.close();
			db.close();
		}
	}

	@Override
	public void onClick(View v) {
		Log.i(TAG, "onClick");
		switch (v.getId()) {
		case R.id.detailseventButtonReturn:
			Log.d(TAG, "finished");
			
			// Close the activity
			finish();
			break;

		case R.id.detailseventButtonDelete:
			if (app.deleteEvent((int) eventId)) {
				Log.d(TAG, "event deleted");
				
				// Set the return code of the activity
				setResult(RESULT_EVENT_DELETED);
				
				// Update the widget
				sendBroadcast(new Intent(EventManagerWidget.REFRESH_WIDGET));
				
				// Close the activity
				finish();
			}
			break;
		}
	}

}
