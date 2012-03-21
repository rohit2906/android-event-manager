package code.eventmanager;

import android.app.Activity;
import android.os.Bundle;

public class NeweventActivity extends Activity {
	
	@SuppressWarnings("unused")
	private static final String TAG = "NeweventActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.newevent_layout);
	}
}
