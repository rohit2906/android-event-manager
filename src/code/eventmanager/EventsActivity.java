package code.eventmanager;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class EventsActivity extends Activity implements OnClickListener, OnSharedPreferenceChangeListener {
	
	private static final String TAG = "EventsActivity";
	
	Button buttonNewEvent;
	Intent pollerServiceIntent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.events_layout);
		
		pollerServiceIntent = null;
		startPoller();
		
		buttonNewEvent = (Button) findViewById(R.id.eventsButtonNewEvent);
		buttonNewEvent.setOnClickListener(this);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		// TODO: remove later, it's just for testing on devices
		stopPoller();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.eventsButtonNewEvent:
			startActivity(new Intent(this, NewEventActivity.class));
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.events_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuItemPreferences:
			startActivity(new Intent(this, PreferencesActivity.class));
			break;
		}
		return true;
	}
	
	private void startPoller() {
		if (pollerServiceIntent == null) {
			pollerServiceIntent = new Intent(this, PollerService.class);
			startService(pollerServiceIntent);
		} else {
			Log.d(TAG, "startPoller(): PollerService is already running.");
		}
	}
	
	private void stopPoller() {
		if (pollerServiceIntent != null) {
			stopService(pollerServiceIntent);
			pollerServiceIntent = null;
		} else {
			Log.d(TAG, "stopPoller(): PollerService is not running.");
		}
	}
}
