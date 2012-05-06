package code.eventmanager;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
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
		Log.i(TAG, "onCreate");
		setContentView(R.layout.credentials_layout);

		app = (EventManagerApp) getApplication();

		btnLogin = (Button) findViewById(R.id.credentialsBtnLogin);
		etUsername = (EditText) findViewById(R.id.credentialsEtUsername);
		etPassword = (EditText) findViewById(R.id.credentialsEtPassword);
		btnLogin.setOnClickListener(this);
	}

	/**
	 * Check whether every field is filled and save the account credentials.
	 */
	@Override
	public void onClick(View v) {
		Log.i(TAG, "onClick");
		String username = etUsername.getText().toString();
		String password = etPassword.getText().toString();
		if (username.isEmpty()) {
			Toast.makeText(this, getText(R.string.credentialsToastMissingUsername),
					Toast.LENGTH_LONG).show();
		} else if (password.isEmpty()) {
			Toast.makeText(this, getText(R.string.credentialsToastMissingPassword),
					Toast.LENGTH_LONG).show();
		} else {
			Editor editor = app.getPrefs().edit();
			
			editor.putBoolean((String) getText(R.string.preferencesKeyDefaultAccount), false);
			editor.putString((String) getText(R.string.preferencesKeyCustomAccountMail), username);
			editor.putString((String) getText(R.string.preferencesKeyCustomAccountPassword), password);
			editor.commit();

			startActivity(new Intent(this, EventsActivity.class));
			
			// close the activity
			finish();
		}
	}

	/**
	 * If back button is pressed, it forces to start the LoginActivity because
	 * it is not in the activity stack by default
	 */
	@Override
	public void onBackPressed() {
		Log.i(TAG, "onBackPressed");
		startActivityIfNeeded(new Intent(this, LoginActivity.class), -1);
	}
}
