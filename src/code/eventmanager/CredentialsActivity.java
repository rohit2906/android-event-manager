package code.eventmanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class CredentialsActivity extends Activity implements OnClickListener {

	Button btnLogin;
	EditText tvUsername;
	EditText tvPassword;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.credentials_layout);
		btnLogin = (Button) findViewById(R.id.credentialsBtnLogin);
		tvUsername = (EditText) findViewById(R.id.credentialsEtUsername);
		tvPassword = (EditText) findViewById(R.id.credentialsEtPassword);
		btnLogin.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (tvUsername.getText().toString() != "" && tvPassword.getText().toString() != "")
			startActivity(new Intent(this, EventsActivity.class));
	}

}
