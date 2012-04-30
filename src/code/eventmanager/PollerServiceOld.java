package code.eventmanager;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class PollerServiceOld extends Service {

	private static final String TAG = PollerServiceOld.class.getSimpleName();

	private Poller thread;
	private EventManagerApp app;
	private int sleeptime;

	/**
	 * Instance the thread
	 */
	@Override
	public void onCreate() {
		app = (EventManagerApp) getApplication();
		Log.v(TAG, "onCreate");
		thread = new Poller();
	}

	/**
	 * Stop the thread and destroy the service
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.v(TAG, "onDestroy");
		thread.stopThread();
		thread.interrupt(); // With only interrupt the thread could continue to
							// run
		thread = null;
	}

	/**
	 * Start the thread
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.v(TAG, "onStartCommand");
		thread.start();
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private class Poller extends Thread {

		final String TAG = Poller.class.getSimpleName();
		private volatile boolean running;

		/**
		 * Instance the thread without start it
		 */
		public Poller() {
			super("Poller");
			running = false;
		}

		/**
		 * Check if running is true and then download the spreadsheets with the
		 * title defined by the user
		 */
		@Override
		public void run() {
			super.run();
			running = true;
			while (running) {
				Log.v(TAG, "Polling...");
				try {
					app.parseEvents();
					sleeptime = Integer
							.parseInt(app.getPrefs().getString((String) getText(R.string.credentialsKeyMinutesBetweenUpdates), "60")) * 60000;
					
					sleep(sleeptime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		/**
		 * Set the running variable to false in order to stop the thread the
		 * next loop iteration
		 */
		public synchronized void stopThread() {
			running = false;
		}
	}
}
