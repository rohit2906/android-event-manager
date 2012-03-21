package code.eventmanager;

import android.app.Activity;
import android.os.Bundle;

public class NewEventActivity extends Activity {
	
	@SuppressWarnings("unused")
	private static final String TAG = NewEventActivity.class.getSimpleName();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.newevent_layout);
	}
}
