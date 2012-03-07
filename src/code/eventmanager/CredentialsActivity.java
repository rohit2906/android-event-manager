package code.eventmanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class CredentialsActivity extends Activity implements OnClickListener {

	Button btnLogin;
	EditText etUsername;
	EditText etPassword;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.credentials_layout);
		btnLogin = (Button) findViewById(R.id.credentialsBtnLogin);
		etUsername = (EditText) findViewById(R.id.credentialsEtUsername);
		etPassword = (EditText) findViewById(R.id.credentialsEtPassword);
		btnLogin.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (etUsername.getText().toString().equalsIgnoreCase(""))
			Toast.makeText(this, "Write your username ", Toast.LENGTH_LONG).show();
			else if( etPassword.getText().toString().equalsIgnoreCase(""))
				Toast.makeText(this, "Write your password ", Toast.LENGTH_LONG).show();
		else{
			startService(new Intent(this, PollerService.class));
			startActivity(new Intent(this, EventsActivity.class));
		}
			
	}
}
