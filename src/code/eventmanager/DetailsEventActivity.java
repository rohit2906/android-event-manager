package code.eventmanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class DetailsEventActivity extends Activity implements OnClickListener {
	
	private static final String TAG = NewEventActivity.class.getSimpleName();
	public static final int CODE_EVENT_DELETED = 5;
	
	EventManagerApp app;
	public DbHelper dbHelper;

	private TextView tvTitle;
	private TextView tvStarting;
	private TextView tvEnding;
	private TextView tvAddress;
	private TextView tvDescription;
	
	private int event_id;
	private String event_creator;

	private Button btnReturn;
	private Button btnDelete;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.detailsevent_layout);
		
		app = (EventManagerApp) getApplication();

		Intent callingIntent = this.getIntent();
		event_id = callingIntent.getIntExtra(EventsActivity.EVENT_DETAILS_ID, -1);
		event_creator = callingIntent.getStringExtra(EventsActivity.EVENT_DETAILS_CREATOR);
		
		tvTitle = (TextView) findViewById(R.id.detailseventTextViewTitle);
		tvTitle.setText(callingIntent.getStringExtra(EventsActivity.EVENT_DETAILS_NAME));
		
		tvStarting = (TextView) findViewById(R.id.detailseventTextViewStarting);
		tvStarting.setText(callingIntent.getStringExtra(EventsActivity.EVENT_DETAILS_STARTING));
		
		tvEnding = (TextView) findViewById(R.id.detailseventTextViewEnding);
		tvEnding.setText(callingIntent.getStringExtra(EventsActivity.EVENT_DETAILS_ENDING));
		
		tvAddress = (TextView) findViewById(R.id.detailseventTextViewAddress);
		tvAddress.setText(callingIntent.getStringExtra(EventsActivity.EVENT_DETAILS_ADDRESS));
		
		tvDescription = (TextView) findViewById(R.id.detailseventTextViewDescription);
		tvDescription.setText(callingIntent.getStringExtra(EventsActivity.EVENT_DETAILS_DESCRIPTION));
		
		btnReturn = (Button) findViewById(R.id.detailseventButtonReturn);
		btnReturn.setOnClickListener(this);
		
		btnDelete = (Button) findViewById(R.id.detailseventButtonDelete);
		btnDelete.setOnClickListener(this);
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
				if (app.getCreator() == event_creator) {
					app.deleteEvent(event_id);
					Log.d(TAG, "event deleted");
					setResult(CODE_EVENT_DELETED);
					finish();
				} else {
					Toast.makeText(this, "You are not the creator of the event.", Toast.LENGTH_LONG).show();
					Log.d(TAG, "Deletion not allowed. User != Creator.");
				}
			}
			break;
		}
	}

}
