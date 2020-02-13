package com.example.nicolaswong.androidassignment;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DB extends SQLiteOpenHelper {
    public static final String TABLE_NAME = "weather";
    public static final String NAME = "name";
    private final static String DATABASE_NAME = "expense.db";
    private final static int DATABASE_VERSION = 1;

    public DB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS weather"+
        "( id INTEGER PRIMARY KEY AUTOINCREMENT,"+
        "name TEXT);");
        db.execSQL("drop table weather");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }

    public void deleteData (String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME,"name LIKE ?",new String[]{name});
    }
}
