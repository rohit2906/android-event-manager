package code.eventmanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class CredentialsActivity extends Activity implements OnClickListener {

	private static final String TAG = CredentialsActivity.class.getSimpleName();

	Button btnLogin;
	EditText etUsername;
	EditText etPassword;
	EventManagerApp app;

	/**
	 * Reference to the widgets in the UI and set the listeners
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.credentials_layout);

		app = (EventManagerApp) getApplication();

		btnLogin = (Button) findViewById(R.id.credentialsBtnLogin);
		etUsername = (EditText) findViewById(R.id.credentialsEtUsername);
		etPassword = (EditText) findViewById(R.id.credentialsEtPassword);
		btnLogin.setOnClickListener(this);
		Log.d(TAG, "onCreate");
	}

	/**
	 * Check whether every field is filled and save the account credentials.
	 */
	@Override
	public void onClick(View v) {
		String username = etUsername.getText().toString();
		String password = etPassword.getText().toString();
		if (username.isEmpty()) {
			Toast.makeText(this, "Write your username", Toast.LENGTH_LONG)
					.show();
		} else if (password.isEmpty()) {
			Toast.makeText(this, "Write your password", Toast.LENGTH_LONG)
					.show();
		} else {
			app.getPrefs()
					.edit()
					.putBoolean(
							(String) getText(R.string.credentialsKeyDefaultAccount),
							false);
			app.getPrefs()
					.edit()
					.putString(
							(String) getText(R.string.credentialsKeyCustomAccountMail),
							username);
			app.getPrefs()
					.edit()
					.putString(
							(String) getText(R.string.credentialsKeyCustomAccountPassword),
							password);
			app.getPrefs().edit().apply();

			startActivity(new Intent(this, EventsActivity.class));
		}
		Log.d(TAG, "onClick");
	}

	/**
	 * If back button is pressed, it forces to start the LoginActivity because
	 * it is not in the activity stack by default
	 */
	@Override
	public void onBackPressed() {
		startActivityIfNeeded(new Intent(this, LoginActivity.class), -1);
		Log.d(TAG, "onBackPressed");
	}
}
