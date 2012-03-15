package code.eventmanager;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class DbHelper extends SQLiteOpenHelper {
	static final String TAG = "DbHelper";
	static final String DB_NAME = "events.db";
	static final int DB_VERSION = 1;
	static final String TABLE_EVENTS = "events";
	static final String TABLE_ATTENDINGS = "attendings";
	static final String EVENTS_ID = BaseColumns._ID;
	static final String EVENTS_NAME = "name";
	static final String EVENTS_DESCRIPTION = "description";
	static final String EVENTS_CREATOR = "creator";
	static final String EVENTS_STARTING_TS = "starting_timestamp";
	static final String EVENTS_ENDING_TS = "ending_timestamp";
	static final String ATTENDINGS_EVENT_ID = "id_events";
	static final String ATTENDINGS_EMAILS = "emails";

	public DbHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql;
		sql = "CREATE TABLE " + TABLE_EVENTS + " (" + EVENTS_ID
				+ " integer NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE, " + EVENTS_NAME
				+ " text NOT NULL, " + EVENTS_DESCRIPTION + " text, " + EVENTS_CREATOR
				+ " text NOT NULL" + EVENTS_STARTING_TS + " integer NOT NULL, "
				+ EVENTS_ENDING_TS + " integer NOT NULL)";
		db.execSQL(sql);
		Log.d(TAG, "onCreated sql: " + sql);
		sql = "CREATE TABLE " + TABLE_ATTENDINGS + " (" + ATTENDINGS_EVENT_ID
				+ " integer NOT NULL REFERENCES " + TABLE_EVENTS + "(" + EVENTS_ID + ") ON DELETE CASCADE ON UPDATE CASCADE, " + ATTENDINGS_EMAILS + " text NOT NULL,PRIMARY KEY (ID_events,Mails))";
		db.execSQL(sql);
		Log.d(TAG, "onCreated sql: " + sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
	}
}
