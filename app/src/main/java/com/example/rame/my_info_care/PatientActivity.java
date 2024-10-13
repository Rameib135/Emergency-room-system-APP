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
    private TextView textViewStatus, textViewRemarks, textViewDischargeStatus;  // Added textViewDischargeStatus
    private DatabaseHelper databaseHelper;

    private ListView listViewStatusHistory;
    private String userIdentity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient);

        textViewStatus = findViewById(R.id.textViewStatus);
        textViewRemarks = findViewById(R.id.textViewRemarks);
        textViewDischargeStatus = findViewById(R.id.textViewDischargeStatus);  // Initialize discharge status TextView
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
            int dischargeIndex = cursor.getColumnIndex("discharge_status");  // Fetch discharge status

            if (statusIndex != -1 && remarksIndex != -1 && dischargeIndex != -1) {
                String status = cursor.getString(statusIndex);
                String remarks = cursor.getString(remarksIndex);
                String dischargeStatus = cursor.getString(dischargeIndex);  // Get discharge status

                textViewStatus.setText(getString(R.string.status_text, status));
                textViewRemarks.setText(getString(R.string.remarks_text, remarks));
                textViewDischargeStatus.setText("Discharge Status: " + dischargeStatus);  // Show discharge status
            }
            cursor.close();
        } else {
            textViewStatus.setText(getString(R.string.status_text, "Not available"));
            textViewRemarks.setText(getString(R.string.remarks_text, "Not available"));
            textViewDischargeStatus.setText("Discharge Status: Not available");
        }
    }

    private void loadStatusHistory() {
        int patientId = getPatientIdByIdentity(userIdentity);  // Get the patient's ID using their identity number
        List<String> statusUpdates = getStatusHistory(patientId);  // Retrieve the status history

        // Set the status history to the ListView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, statusUpdates);
        listViewStatusHistory.setAdapter(adapter);
    }

    // Method to get the status history
    private List<String> getStatusHistory(int patientId) {
        Cursor cursor = databaseHelper.getStatusHistory(patientId);
        List<String> statusUpdates = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                int statusIndex = cursor.getColumnIndex("status_text");
                int remarksIndex = cursor.getColumnIndex("remarks");
                int timestampIndex = cursor.getColumnIndex("timestamp");

                // Check that all columns exist in the cursor
                if (statusIndex != -1 && remarksIndex != -1 && timestampIndex != -1) {
                    String status = cursor.getString(statusIndex);
                    String remarks = cursor.getString(remarksIndex);
                    String timestamp = cursor.getString(timestampIndex);
                    // Add each status update to the history
                    statusUpdates.add("Status: " + status + "\nRemarks: " + remarks + "\nTime: " + timestamp);
                }
            } while (cursor.moveToNext());  // Continue through all records
        }
        cursor.close();  // Close the cursor
        return statusUpdates;
    }

    @SuppressLint("Range")
    private int getPatientIdByIdentity(String identityNumber) {
        Cursor cursor = databaseHelper.getPatientIdByIdentity(identityNumber);
        int patientId = -1;
        if (cursor != null && cursor.moveToFirst()) {
            patientId = cursor.getInt(cursor.getColumnIndex("id"));  // Ensure the ID column matches your database schema
            cursor.close();
        }
        return patientId;
    }
}
