package code.eventmanager;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;

public class PreferencesActivity extends PreferenceActivity {
	
	private static final String TAG = PreferencesActivity.class.getSimpleName();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences_layout);
		Log.v(TAG, "onCreate");
	}
}
