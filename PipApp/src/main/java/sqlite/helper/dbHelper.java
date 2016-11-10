package sqlite.helper;

import sqlite.model.PipSession;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/*
 * dbHelper.java
 * Helper class encapsulating SQLite operations.
 * Created by DED8IRD on 11/7/2016.
 */

public class dbHelper extends SQLiteOpenHelper {

    // Logcat tag
    private static final String LOG = dbHelper.class.getName();

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "PipSession";

    // Table Names
    private static final String TABLE_PIP_SESSIONS = "PIP_Sessions";

    // Common column names
    private static final String KEY_ID = "id";

    // Pip Session Info Table - attributes
    private static final String KEY_PARTICIPANT = "participant";
    private static final String KEY_GSR_VAL = "raw_GSR";
    private static final String KEY_CURRENT_TREND = "current_trend";
    private static final String KEY_ACCUM_TREND = "accum_trend";

    // Table Create Statements
    private static final String CREATE_TABLE_PIP_SESSIONS = "CREATE TABLE "
            + TABLE_PIP_SESSIONS + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_PARTICIPANT + " CHAR(40)," + KEY_GSR_VAL + " DOUBLE," + KEY_CURRENT_TREND
            + " CHAR(20)," + KEY_ACCUM_TREND + " DOUBLE" + ")";

    public dbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // creating required tables
        db.execSQL(CREATE_TABLE_PIP_SESSIONS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PIP_SESSIONS);

        // create new tables
        onCreate(db);
    }

    // ------------------------ "Pip Sessions" table methods ------------------------ //

    /*
     * Creating a Pip Session
     */
    public long addPipSession(PipSession session) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_PARTICIPANT, session.getParticipant());
        values.put(KEY_GSR_VAL, session.getGSR());
        values.put(KEY_CURRENT_TREND, session.getCurrentTrend());
        values.put(KEY_ACCUM_TREND, session.getAccumTrend());

        // insert row
        return db.insert(TABLE_PIP_SESSIONS, null, values);
    }

    /*
     * get single session
     */
    public PipSession getSession(long session_id) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + TABLE_PIP_SESSIONS + " WHERE "
                + KEY_ID + " = " + session_id;

        Log.e(LOG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        if (c != null)
            c.moveToFirst();

        PipSession session = new PipSession();
        session.setId(c.getInt(c.getColumnIndex(KEY_ID)));
        session.setParticipant((c.getString(c.getColumnIndex(KEY_PARTICIPANT))));

        return session;
    }

    /*
     * getting all sessions
     */
    public List<PipSession> getAllSessions() {
        List<PipSession> sessions = new ArrayList<PipSession>();
        String selectQuery = "SELECT  * FROM " + TABLE_PIP_SESSIONS;

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                PipSession session = new PipSession();
                session.setId(c.getInt((c.getColumnIndex(KEY_ID))));
                session.setParticipant((c.getString(c.getColumnIndex(KEY_PARTICIPANT))));
                session.setGSR((c.getDouble(c.getColumnIndex(KEY_GSR_VAL))));
                session.setCurrentTrend((c.getString(c.getColumnIndex(KEY_CURRENT_TREND))));
                session.setAccumTrend((c.getDouble(c.getColumnIndex(KEY_ACCUM_TREND))));

                // adding to session list
                sessions.add(session);
            } while (c.moveToNext());
        }

        return sessions;
    }

    /*
     * Get session count
     */
    public int getSessionCount() {
        String countQuery = "SELECT  * FROM " + TABLE_PIP_SESSIONS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();

        // return count
        return count;
    }

    /*
     * Updating a session
     */
    public int updateSession(PipSession session) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, session.getId());
        values.put(KEY_PARTICIPANT, session.getParticipant());
        values.put(KEY_GSR_VAL, session.getGSR());
        values.put(KEY_CURRENT_TREND, session.getCurrentTrend());
        values.put(KEY_ACCUM_TREND, session.getAccumTrend());

        // updating row
        return db.update(TABLE_PIP_SESSIONS, values, KEY_ID + " = ?",
                new String[] { String.valueOf(session.getId()) });
    }

    /*
     * Deleting a session
     */
    public void deleteSession(long session_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PIP_SESSIONS, KEY_ID + " = ?",
                new String[] { String.valueOf(session_id) });
    }

    /*
     * Close database
     */
    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen())
            db.close();
    }

    /*
     * get datetime
     */
    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }
}
