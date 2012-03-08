package code.eventmanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class LoginActivity extends Activity implements OnClickListener {
	
	private static final String TAG = "LoginActivity";
	
	Button btnLogin;
	Button btnOtherAccount;
	Intent pollerServiceIntent;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_layout);
		
		pollerServiceIntent = null;
		
		btnLogin = (Button) findViewById(R.id.loginBtnLogin);
		btnOtherAccount = (Button) findViewById(R.id.loginBtnOtherAccount);
		btnLogin.setOnClickListener(this);
		btnOtherAccount.setOnClickListener(this);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		// TODO: remove later, it's just for testing on devices
		stopPoller();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.loginBtnLogin:
			startPoller();
			startActivity(new Intent(this, EventsActivity.class));
			break;
			
		case R.id.loginBtnOtherAccount:
			startActivity(new Intent(this, CredentialsActivity.class));
			break;
			
		default:
			break;
		}
	}
	
	private void startPoller() {
		if (pollerServiceIntent == null) {
			pollerServiceIntent = new Intent(this, PollerService.class);
			startService(pollerServiceIntent);
		} else {
			Log.d(TAG, "startPoller(): PollerService is already running.");
		}
	}
	
	private void stopPoller() {
		if (pollerServiceIntent != null) {
			stopService(pollerServiceIntent);
			pollerServiceIntent = null;
		} else {
			Log.d(TAG, "stopPoller(): PollerService is not running.");
		}
	}
}
