package com.example.m1032561.pomodoroprototype;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.m1032561.pomodoroprototype.model.LogModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by M1032561 on 1/3/2017.
 */

public class DBHelper extends SQLiteOpenHelper {
    public static final String tablename = "pomodoro_log";  // tablename
    private static final String id = "ID";  // auto generated ID column
    private static final String actualDur = "actual_dur"; // actual time for work
    private static final String ellapsedDur = "ellapsed_dur";  // ellapsed time to complete work
    private static final String rateDur = "rate_dur";  // time taken to write note
    private static final String rate = "rate";  // rating given by user for work he/she done
    private static final String note = "note";  // note written by user
    private static final String breakTime = "breakTime";  // time taken for break
    private static final String created_at = "created_at";  // date at the time of insertion

    private static final String databasename = "pomodoro_db"; // Dtabasename
    private static final int versioncode = 1; //versioncode of the database

    public DBHelper(Context context) {
        super(context, databasename, null, versioncode);

    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        String query;
        query = "CREATE TABLE IF NOT EXISTS " + tablename + "(" + id + " integer primary key, "
                + actualDur + " text, "
                + ellapsedDur + " text, "
                + rateDur + " text, "
                + rate + " text, "
                + note + " text, "
                + breakTime + " text, "
                + created_at + " text)";
        database.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int i, int i1) {
        String query;
        query = "DROP TABLE IF EXISTS " + tablename;
        database.execSQL(query);
        onCreate(database);
    }

    public ArrayList<HashMap<String, String>> getFullLog() {
        ArrayList<HashMap<String, String>> wordList;
        wordList = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT  * FROM " + tablename;
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("id", cursor.getString(0));
                map.put("actual_dur", cursor.getString(1));
                map.put("ellapsed_dur", cursor.getString(2));
                map.put("rate_dur", cursor.getString(3));
                map.put("rate", cursor.getString(4));
                map.put("note", cursor.getString(5));
                map.put("break_dur", cursor.getString(6));
                map.put("created_at", cursor.getString(7));
                wordList.add(map);
            } while (cursor.moveToNext());
        }

        // return contact list
        return wordList;
    }

    public List<LogModel> getLogModelList() {
        List<LogModel> logModelList;
        logModelList = new ArrayList<LogModel>();
        String selectQuery = "SELECT  * FROM " + tablename;
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                LogModel model = new LogModel();
                model.setId(cursor.getString(0));
                model.setActualDur(cursor.getString(1));
                model.setEllapsedDur(cursor.getString(2));
                model.setRateDur(cursor.getString(3));
                model.setRate(cursor.getString(4));
                model.setNote(cursor.getString(5));
                model.setBreakTime(cursor.getString(6));
                try {
                    model.setCreated_at(new Date(Long.parseLong(cursor.getString(7))));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                logModelList.add(model);
            } while (cursor.moveToNext());
        }
        return logModelList;
    }

    public void deleteRecords() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + tablename + ";");
    }
}
