package code.eventmanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class LoginActivity extends Activity implements OnClickListener {

	private static final String TAG = LoginActivity.class.getSimpleName();

	Button btnLogin;
	Button btnOtherAccount;
	EventManagerApp app;

	/**
	 * Reference to the widgets in the UI and set the listeners
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_layout);

		app = (EventManagerApp) getApplication();

		btnLogin = (Button) findViewById(R.id.loginBtnLogin);
		btnOtherAccount = (Button) findViewById(R.id.loginBtnOtherAccount);
		btnLogin.setOnClickListener(this);
		btnOtherAccount.setOnClickListener(this);
		Log.d(TAG, "onCreate()");
	}

	/**
	 * Handle the onClick events. <code>loginBtnLogin</code> simply start the
	 * EventsActivity because the default account is set when the app start
	 * <code>loginBtnOtherAccount</code> start the CredentialsActivity
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.loginBtnLogin:
			startActivity(new Intent(this, EventsActivity.class));
			Log.d(TAG, "loginBtnLogin onClick()");
			break;

		case R.id.loginBtnOtherAccount:
			startActivityIfNeeded(new Intent(this, CredentialsActivity.class),
					-1);
			Log.d(TAG, "loginBtnOtherAccount onClick()");
			break;
		}
	}
}
