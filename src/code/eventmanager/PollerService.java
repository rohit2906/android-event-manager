package code.eventmanager;

import java.util.ArrayList;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import code.eventmanager.auth.AndroidAuthenticator;

import com.pras.SpreadSheet;
import com.pras.SpreadSheetFactory;

public class PollerService extends Service implements OnSharedPreferenceChangeListener {

	private final String TAG = "PollerService";

	private Poller thread;
	private SharedPreferences preferences;
	private SpreadSheetFactory spreadsheetFactory;
	private String spreadsheetTitle;
	private int sleeptime;

	@Override
	public void onCreate() {
		super.onCreate();
		Log.v(TAG, "onCreate");

		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		preferences.registerOnSharedPreferenceChangeListener(this);

		spreadsheetFactory = null;
		thread = new Poller();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.v(TAG, "onDestroy");
		thread.stopThread();
		thread.interrupt();
		thread = null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.v(TAG, "onStartCommand");
		thread.start();
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {
		spreadsheetFactory = null;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private class Poller extends Thread {

		private final String TAG = "Poller";
		private volatile boolean running;

		public Poller() {
			super("Poller");
			running = false;
		}

		@Override
		public void run() {
			super.run();
			running = true;
			while (getRunning()) {
				Log.v(TAG, "Polling...");
				try {
					ArrayList<SpreadSheet> spreadsheets = getSpreadsheetFactory().getSpreadSheet(spreadsheetTitle, true);
					Log.v(TAG, (spreadsheets == null ? "no" : spreadsheets.size()) + " spreadsheets found");
					sleep(sleeptime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		public synchronized void stopThread() {
			running = false;
		}

		private synchronized boolean getRunning() {
			return running;
		}

		private SpreadSheetFactory getSpreadsheetFactory() {
			if (spreadsheetFactory == null) {

				if (preferences.getBoolean("pollerUseDefaultAccount", true)) {
					spreadsheetFactory = SpreadSheetFactory.getInstance(new AndroidAuthenticator(getApplicationContext()));
				} else {
					String email = preferences.getString("pollerOtherAccountEmail", "");
					String password = preferences.getString("pollerOtherAccountPassword", "");
					spreadsheetFactory = SpreadSheetFactory.getInstance(email, password);
				}

				sleeptime = preferences.getInt("pollerUpdateTime", 60) * 6000;
				spreadsheetTitle = preferences.getString("PollerSpreadsheetTitle", "");
			}
			return spreadsheetFactory;
		}
	}
}
