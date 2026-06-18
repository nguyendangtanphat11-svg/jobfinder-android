package com.example.jobfinderapp.database;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "jobfinder.db";
    private static final int DB_VERSION = 1;

    public DBHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // USERS
        db.execSQL(
                "CREATE TABLE users (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "fullname TEXT NOT NULL," +
                        "email TEXT UNIQUE," +
                        "password TEXT," +
                        "phone TEXT," +
                        "avatar TEXT" +
                        ")"
        );

        // COMPANIES
        db.execSQL(
                "CREATE TABLE companies (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "name TEXT NOT NULL," +
                        "address TEXT," +
                        "website TEXT," +
                        "logo TEXT," +
                        "description TEXT" +
                        ")"
        );

        // CATEGORIES
        db.execSQL(
                "CREATE TABLE categories (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "name TEXT NOT NULL" +
                        ")"
        );

        // JOBS
        db.execSQL(
                "CREATE TABLE jobs (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "title TEXT NOT NULL," +
                        "company_id INTEGER," +
                        "category_id INTEGER," +
                        "salary TEXT," +
                        "location TEXT," +
                        "description TEXT," +
                        "requirement TEXT," +
                        "deadline TEXT" +
                        ")"
        );

        // CVS
        db.execSQL(
                "CREATE TABLE cvs (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "user_id INTEGER," +
                        "education TEXT," +
                        "skills TEXT," +
                        "experience TEXT," +
                        "objective TEXT" +
                        ")"
        );

        // APPLICATIONS
        db.execSQL(
                "CREATE TABLE applications (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "user_id INTEGER," +
                        "job_id INTEGER," +
                        "apply_date TEXT," +
                        "status TEXT" +
                        ")"
        );

        // FAVORITES
        db.execSQL(
                "CREATE TABLE favorites (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "user_id INTEGER," +
                        "job_id INTEGER" +
                        ")"
        );

        // NOTIFICATIONS
        db.execSQL(
                "CREATE TABLE notifications (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "user_id INTEGER," +
                        "title TEXT," +
                        "content TEXT," +
                        "created_at TEXT" +
                        ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS notifications");
        db.execSQL("DROP TABLE IF EXISTS favorites");
        db.execSQL("DROP TABLE IF EXISTS applications");
        db.execSQL("DROP TABLE IF EXISTS cvs");
        db.execSQL("DROP TABLE IF EXISTS jobs");
        db.execSQL("DROP TABLE IF EXISTS categories");
        db.execSQL("DROP TABLE IF EXISTS companies");
        db.execSQL("DROP TABLE IF EXISTS users");

        onCreate(db);
    }
    public boolean insertUser(String fullname, String email, String password) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("fullname", fullname);
        values.put("email", email);
        values.put("password", password);

        long result = db.insert("users", null, values);

        return result != -1;
    }
}