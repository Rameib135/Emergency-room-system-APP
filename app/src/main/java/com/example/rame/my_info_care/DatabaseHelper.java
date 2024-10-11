package com.example.rame.my_info_care;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "UserDatabase.db";
    private static final int DATABASE_VERSION = 2;

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

        String CREATE_STATUS_HISTORY_TABLE = "CREATE TABLE status_history ("
                + "status_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "patient_id INTEGER, "
                + "status_text TEXT, "
                + "remarks TEXT, "
                + "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, "
                + "FOREIGN KEY(patient_id) REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + "))";
        db.execSQL(CREATE_STATUS_HISTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ROOM_STATUS);
        db.execSQL("DROP TABLE IF EXISTS status_history");
        onCreate(db);
    }

    // הוספת משתמש חדש
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
        db.close();  // סגירת מסד הנתונים
        return result != -1;
    }

    // בדיקה האם משתמש קיים לפי אימייל וסיסמה
    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_EMAIL + " = ? AND " + COLUMN_PASSWORD + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email, password});
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        db.close();
        return exists;
    }

    // קבלת סוג המשתמש לפי אימייל
    public String getUserType(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_USER_TYPE + " FROM " + TABLE_USERS + " WHERE " + COLUMN_EMAIL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email});
        String userType = null;
        if (cursor != null && cursor.moveToFirst()) {
            userType = cursor.getString(0);
            cursor.close();
        }
        db.close();
        return userType;
    }

    // קבלת מספר תעודת זהות לפי אימייל
    public String getUserIdentity(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_IDENTITY_NUMBER + " FROM " + TABLE_USERS + " WHERE " + COLUMN_EMAIL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email});
        String userIdentity = null;
        if (cursor != null && cursor.moveToFirst()) {
            userIdentity = cursor.getString(0);
            cursor.close();
        }
        db.close();
        return userIdentity;
    }

    // עדכון סטטוס מטופל והוספתו להיסטוריית הסטטוסים
    public boolean updatePatientStatus(String identityNumber, String status, String remarks) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();  // טרנזקציה

        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_STATUS, status);
            values.put(COLUMN_REMARKS, remarks);

            int result = db.update(TABLE_USERS, values, COLUMN_IDENTITY_NUMBER + " = ?", new String[]{identityNumber});

            // הוספת הסטטוס להיסטוריה אם העדכון בוצע בהצלחה
            if (result > 0) {
                Cursor cursor = getPatientIdByIdentity(identityNumber);
                if (cursor != null && cursor.moveToFirst()) {
                    @SuppressLint("Range") int patientId = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
                    ContentValues statusValues = new ContentValues();
                    statusValues.put("patient_id", patientId);
                    statusValues.put("status_text", status);
                    statusValues.put("remarks", remarks);
                    db.insert("status_history", null, statusValues);
                    cursor.close();
                }
            }

            db.setTransactionSuccessful();  // אם הכל עבר בהצלחה
        } finally {
            db.endTransaction();  // סיום טרנזקציה
        }

        db.close();  // סגירת מסד הנתונים
        return true;
    }

    // שליפת היסטוריית סטטוסים לפי מזהה מטופל
    public Cursor getStatusHistory(int patientId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT status_text, remarks, timestamp FROM status_history WHERE patient_id = ? ORDER BY timestamp ASC",
                new String[]{String.valueOf(patientId)});
        return cursor;
    }

    public Cursor getPatientStatus(String identityNumber) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_STATUS + ", " + COLUMN_REMARKS + " FROM " + TABLE_USERS + " WHERE " + COLUMN_IDENTITY_NUMBER + " = ?";
        return db.rawQuery(query, new String[]{identityNumber});
    }


    // שליפת מזהה מטופל לפי תעודת זהות
    public Cursor getPatientIdByIdentity(String identityNumber) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_USERS, new String[]{COLUMN_ID}, COLUMN_IDENTITY_NUMBER + " = ?", new String[]{identityNumber}, null, null, null);
    }

    // הוספת סטטוס חדר
    public boolean addRoomStatus(String time, String event) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ROOM_TIME, time);
        values.put(COLUMN_ROOM_EVENT, event);

        long result = db.insert(TABLE_ROOM_STATUS, null, values);
        db.close();
        return result != -1;
    }

    // בדיקה אם משתמש קיים לפי אימייל
    public boolean checkUserExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_EMAIL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email});
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        db.close();
        return exists;
    }

    // שליפת סטטוס חדר
    public Cursor getRoomStatus() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_ROOM_STATUS, null);
    }
}
