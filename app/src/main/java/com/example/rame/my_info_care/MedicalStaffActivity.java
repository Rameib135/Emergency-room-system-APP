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


public class MedicalStaffActivity extends AppCompatActivity {
    private EditText editTextPatientIdentity, editTextRemarks, editTextRoomTime, editTextRoomEvent;
    private Spinner spinnerStatus;
    private ListView listViewRoomStatus;
    private DatabaseHelper databaseHelper;

    private static final String TAG = "MedicalStaffActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_staff);

        editTextPatientIdentity = findViewById(R.id.editTextPatientIdentity);
        editTextRemarks = findViewById(R.id.editTextRemarks);
        editTextRoomTime = findViewById(R.id.editTextRoomTime);
        editTextRoomEvent = findViewById(R.id.editTextRoomEvent);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        listViewRoomStatus = findViewById(R.id.listViewRoomStatus);
        databaseHelper = new DatabaseHelper(this);

        loadRoomStatus();
    }

    public void updatePatientStatus(View view) {
        String identityNumber = editTextPatientIdentity.getText().toString().trim();
        String status = spinnerStatus.getSelectedItem().toString();
        String remarks = editTextRemarks.getText().toString().trim();

        Log.d(TAG, "Updating patient status: " + identityNumber + ", " + status + ", " + remarks);

        if (identityNumber.isEmpty() || status.isEmpty()) {
            Toast.makeText(this, "Please fill out all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Call the updated method to update both the patient’s current status and status history
        boolean isUpdated = databaseHelper.updatePatientStatus(identityNumber, status, remarks);

        if (isUpdated) {
            Toast.makeText(this, "Patient status updated successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to update patient status", Toast.LENGTH_SHORT).show();
        }
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
            loadRoomStatus(); // Reload the room status to reflect the new entry
        } else {
            Toast.makeText(this, "Failed to add room status", Toast.LENGTH_SHORT).show();
        }
    }

    public void viewSchedule(View view) {
        Log.d(TAG, "Viewing schedule");
        Intent intent = new Intent(MedicalStaffActivity.this, ScheduleActivity.class);
        startActivity(intent);
    }

    private void loadRoomStatus() {
        Log.d(TAG, "Loading room status");
        Cursor cursor = databaseHelper.getRoomStatus();
        String[] from = new String[]{"time", "event"};
        int[] to = new int[]{android.R.id.text1, android.R.id.text2};

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                android.R.layout.two_line_list_item,
                cursor,
                from,
                to,
                0);

        listViewRoomStatus.setAdapter(adapter);
    }

}