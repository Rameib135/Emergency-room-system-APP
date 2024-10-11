package com.example.rame.my_info_care;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import java.util.ArrayList;
import java.util.List;

public class PatientActivity extends AppCompatActivity {
    private TextView textViewStatus, textViewRemarks;
    private DatabaseHelper databaseHelper;

    private ListView listViewStatusHistory;
    private String userIdentity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient);

        textViewStatus = findViewById(R.id.textViewStatus);
        textViewRemarks = findViewById(R.id.textViewRemarks);
        listViewStatusHistory = findViewById(R.id.listViewStatusHistory);
        databaseHelper = new DatabaseHelper(this);

        userIdentity = getIntent().getStringExtra("USER_IDENTITY");

        loadPatientStatus();
        loadStatusHistory();
    }

    private void loadPatientStatus() {
        Cursor cursor = databaseHelper.getPatientStatus(userIdentity);
        if (cursor != null && cursor.moveToFirst()) {
            int statusIndex = cursor.getColumnIndex("status");
            int remarksIndex = cursor.getColumnIndex("remarks");

            if (statusIndex != -1 && remarksIndex != -1) {
                String status = cursor.getString(statusIndex);
                String remarks = cursor.getString(remarksIndex);
                textViewStatus.setText(getString(R.string.status_text, status));
                textViewRemarks.setText(getString(R.string.remarks_text, remarks));
            } else {
                Log.e("PatientActivity", "Column not found");
                textViewStatus.setText(getString(R.string.status_text, "Not available"));
                textViewRemarks.setText(getString(R.string.remarks_text, "Not available"));
            }
            cursor.close();
        } else {
            textViewStatus.setText(getString(R.string.status_text, "Not available"));
            textViewRemarks.setText(getString(R.string.remarks_text, "Not available"));
        }
    }

    private void loadStatusHistory() {
        int patientId = getPatientIdByIdentity(userIdentity);
        List<String> statusUpdates = getStatusHistory(patientId);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, statusUpdates);
        listViewStatusHistory.setAdapter(adapter);
    }

    private List<String> getStatusHistory(int patientId) {
        Cursor cursor = databaseHelper.getStatusHistory(patientId);
        List<String> statusUpdates = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                int statusIndex = cursor.getColumnIndex("status_text");
                int remarksIndex = cursor.getColumnIndex("remarks");
                int timestampIndex = cursor.getColumnIndex("timestamp");

                if (statusIndex != -1 && remarksIndex != -1 && timestampIndex != -1) {
                    String status = cursor.getString(statusIndex);
                    String remarks = cursor.getString(remarksIndex);
                    String timestamp = cursor.getString(timestampIndex);
                    // הוספת כל רשומה לרשימת ההיסטוריה
                    statusUpdates.add("Status: " + status + "\nRemarks: " + remarks + "\nTime: " + timestamp);
                } else {
                    Log.e("PatientActivity", "One of the columns is missing.");
                }
            } while (cursor.moveToNext());  // המשך בלולאה עד שכל התוצאות הוצגו
        }
        cursor.close();
        return statusUpdates;
    }

    @SuppressLint("Range")
    private int getPatientIdByIdentity(String identityNumber) {
        Cursor cursor = databaseHelper.getPatientIdByIdentity(identityNumber);
        int patientId = -1;
        if (cursor != null && cursor.moveToFirst()) {
            patientId = cursor.getInt(cursor.getColumnIndex("id")); // ודא שעמודת ה-ID תואמת למסד הנתונים שלך
            cursor.close();
        }
        return patientId;
    }
}
