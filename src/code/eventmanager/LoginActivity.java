package code.eventmanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class LoginActivity extends Activity implements OnClickListener {

	Button btnLogin;
	Button btnOtherAccount;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_layout);
		btnLogin = (Button) findViewById(R.id.loginBtnLogin);
		btnOtherAccount = (Button) findViewById(R.id.loginBtnOtherAccount);
		btnLogin.setOnClickListener(this);
		btnOtherAccount.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.loginBtnLogin:
			startActivity(new Intent(this, EventsActivity.class));
			break;
		case R.id.loginBtnOtherAccount:
			startActivity(new Intent(this, CredentialsActivity.class));
			break;
		default:
			break;
		}
	}
}
