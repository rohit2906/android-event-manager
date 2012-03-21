package code.eventmanager;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class LoginActivity extends Activity implements OnClickListener {

	private static final String TAG = LoginActivity.class.getSimpleName();

	Button btnLogin;
	Button btnOtherAccount;
	EventManagerApp app;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_layout);

		app=(EventManagerApp)getApplication();

		btnLogin = (Button) findViewById(R.id.loginBtnLogin);
		btnOtherAccount = (Button) findViewById(R.id.loginBtnOtherAccount);
		btnLogin.setOnClickListener(this);
		btnOtherAccount.setOnClickListener(this);
		Log.d(TAG, "onCreate()");
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.loginBtnLogin:
			app.getPrefs().edit().putBoolean("useDefaultAccount", true);
			app.getPrefs().edit().apply();
			startActivity(new Intent(this, EventsActivity.class));
			Log.d(TAG, "loginBtnLogin onClick()");
			break;

		case R.id.loginBtnOtherAccount:
			startActivityIfNeeded(new Intent(this, CredentialsActivity.class), -1);
			Log.d(TAG, "loginBtnOtherAccount onClick()");
			break;
		}
	}
}
