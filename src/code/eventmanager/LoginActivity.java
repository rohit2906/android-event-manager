package code.eventmanager;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
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
		Log.i(TAG, "onCreate");
		setContentView(R.layout.login_layout);

		app = (EventManagerApp) getApplication();

		btnLogin = (Button) findViewById(R.id.loginBtnLogin);
		btnOtherAccount = (Button) findViewById(R.id.loginBtnOtherAccount);
		btnLogin.setOnClickListener(this);
		btnOtherAccount.setOnClickListener(this);

	}

	/**
	 * Handle the onClick events. <code>loginBtnLogin</code> simply start the
	 * EventsActivity because the default account is set when the app start
	 * <code>loginBtnOtherAccount</code> start the CredentialsActivity
	 */
	@Override
	public void onClick(View v) {
		Log.i(TAG, "onClick");
		switch (v.getId()) {
		case R.id.loginBtnLogin:
			Log.d(TAG, "loginBtnLogin onClick()");
			SharedPreferences.Editor editor = app.getPrefs().edit();
			editor.putBoolean(
					(String) getText(R.string.credentialsKeyDefaultAccount),
					true);
			editor.commit();
			startActivity(new Intent(this, EventsActivity.class));
			break;

		case R.id.loginBtnOtherAccount:
			Log.d(TAG, "loginBtnOtherAccount onClick()");
			startActivity(new Intent(this, CredentialsActivity.class));
			break;
		}
	}
}
