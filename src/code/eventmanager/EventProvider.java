package code.eventmanager;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class EventProvider extends ContentProvider {
	private static final String TAG = EventProvider.class.getSimpleName();

	public static final Uri CONTENT_URI = Uri.parse("content://code.eventmanager.eventprovider");
	public static final String SINGLE_RECORD_MIME_TYPE = "vnd.android.cursor.item/vnd.code.eventmanager.event";
	public static final String MULTIPLE_RECORDS_MIME_TYPE = "vnd.android.cursor.dir/vnd.code.eventmanager.event";

	DbHelper dbHelper;

	@Override
	public boolean onCreate() {
		dbHelper = new DbHelper(getContext());
		return true;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		Log.d(TAG, "insert");
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			long id = db.insertOrThrow(DbHelper.TABLE_EVENTS, null, values);
			if (id == -1) {
				throw new RuntimeException(String.format(
						"%s: Failed to insert [%s] to [%s] for unknown reasons.", TAG, values, uri));
			} else {
				// If the insert was successful, we use the ContentUris.withAppendedId() helper method
				// to craft a new URI containing the ID of the new record appended to the standard
				// provider’s URI.
				Uri newUri = ContentUris.withAppendedId(uri, id);

				// Notify the Context's ContentResolver of the change
				getContext().getContentResolver().notifyChange(newUri, null);
				return newUri;
			}
		} finally {
			db.close();
		}
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		Log.d(TAG, "update");
		long id = this.getId(uri);
		int count;
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		try {
			if (id < 0)
				count = db.update(DbHelper.TABLE_EVENTS, values, selection, selectionArgs);
			else
				count = db.update(DbHelper.TABLE_EVENTS, values, DbHelper.EVENT_ID + "=?", new String[] { Long.toString(id) });
		} finally {
			db.close();
		}

		// Notify the Context's ContentResolver of the change
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		Log.d(TAG, "delete");
		long id = this.getId(uri);
		int count;
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		try {
			if (id < 0) {
				count = db.delete(DbHelper.TABLE_EVENTS, selection, selectionArgs);
			} else {
				count = db.delete(DbHelper.TABLE_EVENTS, DbHelper.EVENT_ID + "=?", new String[] { Long.toString(id) });
			}
		} finally {
			db.close();
		}

		// Notify the Context's ContentResolver of the change
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		Log.d(TAG, "query");
		long id = this.getId(uri);
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor c;

		if (id < 0) {
			c = db.query(DbHelper.TABLE_EVENTS, projection, selection, selectionArgs,
					null, null, sortOrder);
		} else {
			c = db.query(DbHelper.TABLE_EVENTS, projection, DbHelper.EVENT_ID + "=?", new String[] { Long.toString(id) }, null, null, null);
		}

		// Notify the context's ContentResolver if the cursor result set changes
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	// Helper method to extract ID from Uri
	private long getId(Uri uri) {
		long result = -1;
		String lastPathSegment = uri.getLastPathSegment();
		if (lastPathSegment != null) {
			try {
				result = Long.parseLong(lastPathSegment);
			} catch (NumberFormatException e) {
				result = -2;
			}
		}
		return result;
	}

	@Override
	public String getType(Uri uri) {
		return (getId(uri) < 0 ? MULTIPLE_RECORDS_MIME_TYPE : SINGLE_RECORD_MIME_TYPE);
	}

}
