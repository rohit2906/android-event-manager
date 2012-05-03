package code.eventmanager;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class DbHelper extends SQLiteOpenHelper {

	private static final String TAG = DbHelper.class.getSimpleName();

	public static final String DB_NAME = "EventManager.db";
	public static int DB_VERSION = 1;
	public static final String TABLE_EVENTS = "events";
	public static final String EVENT_ID = BaseColumns._ID;
	public static final String EVENT_NAME = "name";
	public static final String EVENT_ADDRESS = "address";
	public static final String EVENT_DESCRIPTION = "description";
	public static final String EVENT_CREATOR = "creator";
	public static final String EVENT_STARTING_TS = "starting_timestamp";
	public static final String EVENT_ENDING_TS = "ending_timestamp";
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

		sql = "CREATE TABLE " + TABLE_EVENTS + " (" + EVENT_ID
				+ " integer NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE, "
				+ EVENT_NAME + " text NOT NULL, " + EVENT_ADDRESS + " text, "
				+ EVENT_DESCRIPTION + " text, " + EVENT_CREATOR
				+ " text NOT NULL, " + EVENT_STARTING_TS
				+ " integer NOT NULL, " + EVENT_ENDING_TS
				+ " integer NOT NULL)";

		db.execSQL(sql);
		Log.v(TAG, "SQL executed: " + sql);
	}

	/**
	 * Handle the future version of the database
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.i(TAG, "onUpgrade");
		db.execSQL("drop table if exists " + TABLE_EVENTS);
		onCreate(db);
		DB_VERSION = newVersion;
	}

	/**
	 * @return Cursor with all the events
	 */
	public Cursor getAllEvents() {
		SQLiteDatabase db = getReadableDatabase();
		return db.query(TABLE_EVENTS, null, null, null, null, null, EVENT_STARTING_TS + " DESC");
	}
}
