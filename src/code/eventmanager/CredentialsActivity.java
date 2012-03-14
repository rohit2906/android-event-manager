package code.eventmanager;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class CredentialsActivity extends Activity implements OnClickListener {

	@SuppressWarnings("unused")
	private static final String TAG = "CredentialsActivity";

	Button btnLogin;
	EditText etUsername;
	EditText etPassword;
	private SharedPreferences.Editor preferencesEditor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.credentials_layout);

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		preferencesEditor = preferences.edit();

		btnLogin = (Button) findViewById(R.id.credentialsBtnLogin);
		etUsername = (EditText) findViewById(R.id.credentialsEtUsername);
		etPassword = (EditText) findViewById(R.id.credentialsEtPassword);
		btnLogin.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		String username = etUsername.getText().toString();
		String password = etPassword.getText().toString();
		if (username.isEmpty()) {
			Toast.makeText(this, "Write your username", Toast.LENGTH_LONG).show();
		} else if (password.isEmpty()) {
			Toast.makeText(this, "Write your password", Toast.LENGTH_LONG).show();
		} else {
			preferencesEditor.putBoolean("useDefaultAccount", false);
			preferencesEditor.putString("customAccountEmail", username);
			preferencesEditor.putString("customAccountPassword", password);
			preferencesEditor.apply();
			
			startActivity(new Intent(this, EventsActivity.class));
		}
	}

	@Override
	public void onBackPressed() {
		startActivityIfNeeded(new Intent(this, LoginActivity.class), -1);
	}
}
