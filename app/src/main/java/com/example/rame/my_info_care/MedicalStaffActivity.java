package com.example.rame.my_info_care;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.CheckBox;

public class MedicalStaffActivity extends AppCompatActivity {
    private EditText editTextPatientIdentity, editTextRemarks, editTextRoomTime, editTextRoomEvent;
    private Spinner spinnerStatus;
    private CheckBox checkboxDischarge;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_staff);

        editTextPatientIdentity = findViewById(R.id.editTextPatientIdentity);
        editTextRemarks = findViewById(R.id.editTextRemarks);
        editTextRoomTime = findViewById(R.id.editTextRoomTime);
        editTextRoomEvent = findViewById(R.id.editTextRoomEvent);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        checkboxDischarge = findViewById(R.id.checkboxDischarge);

        databaseHelper = new DatabaseHelper(this);

        loadRoomStatus();
    }

    public void updatePatientStatus(View view) {
        String identityNumber = editTextPatientIdentity.getText().toString().trim();
        String status = spinnerStatus.getSelectedItem().toString();
        String remarks = editTextRemarks.getText().toString().trim();
        String dischargeStatus = checkboxDischarge.isChecked() ? "Discharged" : "In Hospital";

        if (identityNumber.isEmpty() || status.isEmpty()) {
            Toast.makeText(this, "Please fill out all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isUpdated = databaseHelper.updatePatientStatus(identityNumber, status, remarks, dischargeStatus);

        if (isUpdated) {
            Toast.makeText(this, "Patient status updated successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to update patient status", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadRoomStatus() {
        Cursor cursor = databaseHelper.getRoomStatus();
        String[] from = new String[]{"time", "event"};
        int[] to = new int[]{android.R.id.text1, android.R.id.text2};

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                android.R.layout.two_line_list_item,
                cursor,
                from,
                to,
                0);

        ListView listViewRoomStatus = findViewById(R.id.listViewRoomStatus);
        listViewRoomStatus.setAdapter(adapter);
    }

    public void addRoomStatus(View view) {
        String time = editTextRoomTime.getText().toString().trim();
        String event = editTextRoomEvent.getText().toString().trim();

        if (time.isEmpty() || event.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isAdded = databaseHelper.addRoomStatus(time, event);

        if (isAdded) {
            Toast.makeText(this, "Room status added successfully", Toast.LENGTH_SHORT).show();
            loadRoomStatus();
        } else {
            Toast.makeText(this, "Failed to add room status", Toast.LENGTH_SHORT).show();
        }
    }

    public void viewSchedule(View view) {
        Intent intent = new Intent(MedicalStaffActivity.this, ScheduleActivity.class);
        startActivity(intent);
    }

    public void viewPatientAnalytics(View view) {
        Intent intent = new Intent(this, MedicalAnalyticsActivity.class);
        startActivity(intent);
    }

}
