package com.example.rame.my_info_care;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class PatientActivity extends AppCompatActivity {
    private TextView textViewStatus, textViewRemarks;
    private DatabaseHelper databaseHelper;
    private String userIdentity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient);

        textViewStatus = findViewById(R.id.textViewStatus);
        textViewRemarks = findViewById(R.id.textViewRemarks);
        databaseHelper = new DatabaseHelper(this);

        userIdentity = getIntent().getStringExtra("USER_IDENTITY");

        loadPatientStatus();
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
}
