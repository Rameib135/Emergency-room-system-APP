package com.example.rame.my_info_care;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "UserDatabase.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_FIRST_NAME = "firstName";
    private static final String COLUMN_LAST_NAME = "lastName";
    private static final String COLUMN_IDENTITY_NUMBER = "identityNumber";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PHONE = "phone";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_USER_TYPE = "userType";
    private static final String COLUMN_STATUS = "status";
    private static final String COLUMN_REMARKS = "remarks";

    private static final String TABLE_ROOM_STATUS = "room_status";
    private static final String COLUMN_ROOM_ID = "_id";
    private static final String COLUMN_ROOM_TIME = "time";
    private static final String COLUMN_ROOM_EVENT = "event";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_FIRST_NAME + " TEXT, "
                + COLUMN_LAST_NAME + " TEXT, "
                + COLUMN_IDENTITY_NUMBER + " TEXT UNIQUE, "
                + COLUMN_EMAIL + " TEXT UNIQUE, "
                + COLUMN_PHONE + " TEXT, "
                + COLUMN_PASSWORD + " TEXT, "
                + COLUMN_USER_TYPE + " TEXT, "
                + COLUMN_STATUS + " TEXT, "
                + COLUMN_REMARKS + " TEXT)";
        db.execSQL(CREATE_USERS_TABLE);

        String CREATE_ROOM_STATUS_TABLE = "CREATE TABLE " + TABLE_ROOM_STATUS + " ("
                + COLUMN_ROOM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_ROOM_TIME + " TEXT, "
                + COLUMN_ROOM_EVENT + " TEXT)";
        db.execSQL(CREATE_ROOM_STATUS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ROOM_STATUS);
        onCreate(db);
    }

    public boolean addUser(String firstName, String lastName, String identityNumber, String email, String phone, String password, String userType) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FIRST_NAME, firstName);
        values.put(COLUMN_LAST_NAME, lastName);
        values.put(COLUMN_IDENTITY_NUMBER, identityNumber);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PHONE, phone);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_USER_TYPE, userType);

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_EMAIL + " = ? AND " + COLUMN_PASSWORD + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email, password});
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }

    public String getUserType(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_USER_TYPE + " FROM " + TABLE_USERS + " WHERE " + COLUMN_EMAIL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email});
        if (cursor != null && cursor.moveToFirst()) {
            String userType = cursor.getString(0);
            cursor.close();
            return userType;
        }
        return null;
    }

    public String getUserIdentity(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_IDENTITY_NUMBER + " FROM " + TABLE_USERS + " WHERE " + COLUMN_EMAIL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email});
        if (cursor != null && cursor.moveToFirst()) {
            String userIdentity = cursor.getString(0);
            cursor.close();
            return userIdentity;
        }
        return null;
    }

    public boolean updatePatientStatus(String identityNumber, String status, String remarks) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_STATUS, status);
        values.put(COLUMN_REMARKS, remarks);

        int result = db.update(TABLE_USERS, values, COLUMN_IDENTITY_NUMBER + " = ?", new String[]{identityNumber});
        return result > 0;
    }

    public Cursor getRoomStatus() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_ROOM_STATUS, null);
    }

    public Cursor getPatientStatus(String identityNumber) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_STATUS + ", " + COLUMN_REMARKS + " FROM " + TABLE_USERS + " WHERE " + COLUMN_IDENTITY_NUMBER + " = ?";
        return db.rawQuery(query, new String[]{identityNumber});
    }

    public boolean checkUserExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_EMAIL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email});
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }

    public boolean addRoomStatus(String time, String event) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ROOM_TIME, time);
        values.put(COLUMN_ROOM_EVENT, event);

        long result = db.insert(TABLE_ROOM_STATUS, null, values);
        return result != -1;
    }

}
