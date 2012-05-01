package code.eventmanager;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class DbHelper extends SQLiteOpenHelper {

	private static final String TAG = DbHelper.class.getSimpleName();

	public static final String DB_NAME = "EventManager.db";
	public static int DB_VERSION = 1;
	public static final String TABLE_EVENTS = "events";
	public static final String TABLE_ATTENDINGS = "attendings";
	public static final String EVENTS_ID = BaseColumns._ID; // why???
	public static final String EVENTS_NAME = "name";
	public static final String EVENTS_ADDRESS = "address";
	public static final String EVENTS_DESCRIPTION = "description";
	public static final String EVENTS_CREATOR = "creator";
	public static final String EVENTS_STARTING_TS = "starting_timestamp";
	public static final String EVENTS_ENDING_TS = "ending_timestamp";
	public static final String ATTENDINGS_EVENT_ID = "id_events";
	public static final String ATTENDINGS_EMAILS = "emails";
	public Context context;

	public DbHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		this.context = context;
	}

	/**
	 * Create the tables of the database
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.i(TAG, "onCreated");
		String sql;
		sql = "CREATE TABLE " + TABLE_EVENTS + " (" + EVENTS_ID
				+ " integer NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE, "
				+ EVENTS_NAME + " text NOT NULL, " + EVENTS_ADDRESS + " text, "
				+ EVENTS_DESCRIPTION + " text, " + EVENTS_CREATOR
				+ " text NOT NULL, " + EVENTS_STARTING_TS
				+ " integer NOT NULL, " + EVENTS_ENDING_TS
				+ " integer NOT NULL)";
		db.execSQL(sql);
		Log.v(TAG, "SQL executed: " + sql);
		sql = "CREATE TABLE " + TABLE_ATTENDINGS + " (" + ATTENDINGS_EVENT_ID
				+ " integer NOT NULL REFERENCES " + TABLE_EVENTS + "("
				+ EVENTS_ID + ") ON DELETE CASCADE ON UPDATE CASCADE, "
				+ ATTENDINGS_EMAILS + " text NOT NULL, PRIMARY KEY ("
				+ ATTENDINGS_EVENT_ID + "," + ATTENDINGS_EMAILS + "))";
		db.execSQL(sql);
		Log.v(TAG, "SQL executed: " + sql);
	}

	/**
	 * Handle the future version of the database
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.i(TAG, "onUpgrade");
		db.execSQL("drop table if exists " + TABLE_ATTENDINGS);
		db.execSQL("drop table if exists " + TABLE_EVENTS);
		Log.d(TAG, "onUpdated");
		onCreate(db);
		DB_VERSION = newVersion;
	}
}
