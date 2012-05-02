package code.eventmanager;

import java.util.ArrayList;
import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.pras.SpreadSheet;
import com.pras.WorkSheet;

public class DetailsEventActivity extends Activity implements OnClickListener {

	private TextView tvTitle;
	private TextView tvStarting;
	private TextView tvEnding;
	private TextView tvAddress;
	private TextView tvDescription;

	// get the information about the fields from the intent
	private int event_id = getIntent().getIntExtra(
			EventsActivity.EVENT_DETAILS_ID, -1);
	private String event_name = getIntent().getStringExtra(
			EventsActivity.EVENT_DETAILS_NAME);
	private String event_address = getIntent().getStringExtra(
			EventsActivity.EVENT_DETAILS_ADDRESS);
	private String event_description = getIntent().getStringExtra(
			EventsActivity.EVENT_DETAILS_DESCRIPTION);
	private String event_creator = getIntent().getStringExtra(
			EventsActivity.EVENT_DETAILS_CREATOR);
	private String event_starting = getIntent().getStringExtra(
			EventsActivity.EVENT_DETAILS_STARTING);
	private String event_ending = getIntent().getStringExtra(
			EventsActivity.EVENT_DETAILS_ENDING);

	private Button btnReturn;
	private Button btnDelete;

	public DbHelper dbHelper;

	private static final String TAG = NewEventActivity.class.getSimpleName();

	EventManagerApp app;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.detailsevent_layout);

		app = (EventManagerApp) getApplication();
		tvTitle = (TextView) findViewById(R.id.detailseventTextViewTitle);
		tvTitle.setText(event_name);
		tvStarting = (TextView) findViewById(R.id.detailseventTextViewStarting);
		tvStarting.setText(event_starting);
		tvEnding = (TextView) findViewById(R.id.detailseventTextViewEnding);
		tvEnding.setText(event_ending);
		tvAddress = (TextView) findViewById(R.id.detailseventTextViewAddress);
		tvAddress.setText(event_address);
		tvDescription = (TextView) findViewById(R.id.detailseventTextViewDescription);
		tvDescription.setText(event_description);
		btnReturn = (Button) findViewById(R.id.detailseventButtonReturn);
		btnReturn.setOnClickListener(this);
		btnDelete = (Button) findViewById(R.id.detailseventButtonDelete);

		// if the creator of the event is also the user, we'll implement the
		// delete button
		if (app.getCreator() == event_creator)
			btnDelete.setOnClickListener(this);
		else
			btnDelete.setEnabled(false);
	}

	@Override
	public void onClick(View v) {
		Log.i(TAG, "onClick");
		switch (v.getId()) {
		case R.id.detailseventButtonReturn:
			Log.d(TAG, "finished");
			finish();
			break;
		case R.id.detailseventButtonDelete:
			if (this.event_id != -1) {
				app.deleteEvent(event_id);
				Log.d(TAG, "event deleted");
			}
			finish();
			break;
		default:
			break;
		}
	}

}
