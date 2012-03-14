package code.eventmanager;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class LoginActivity extends Activity implements OnClickListener {

	@SuppressWarnings("unused")
	private static final String TAG = "LoginActivity";

	Button btnLogin;
	Button btnOtherAccount;
	private SharedPreferences.Editor preferencesEditor;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_layout);

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		preferencesEditor = preferences.edit();

		btnLogin = (Button) findViewById(R.id.loginBtnLogin);
		btnOtherAccount = (Button) findViewById(R.id.loginBtnOtherAccount);
		btnLogin.setOnClickListener(this);
		btnOtherAccount.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.loginBtnLogin:
			preferencesEditor.putBoolean("useDefaultAccount", true);
			preferencesEditor.apply();
			startActivity(new Intent(this, EventsActivity.class));
			break;

		case R.id.loginBtnOtherAccount:
			startActivityIfNeeded(new Intent(this, CredentialsActivity.class), -1);
			break;
		}
	}
}
