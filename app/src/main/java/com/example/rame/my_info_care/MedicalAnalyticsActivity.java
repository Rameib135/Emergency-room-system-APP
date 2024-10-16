package com.example.rame.my_info_care;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MedicalAnalyticsActivity extends AppCompatActivity {
    private DatabaseHelper databaseHelper;
    private TextView textViewCriticalCount, textViewStableCount, textViewObservationCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_analytics);

        textViewCriticalCount = findViewById(R.id.textViewCriticalCount);
        textViewStableCount = findViewById(R.id.textViewStableCount);
        textViewObservationCount = findViewById(R.id.textViewObservationCount);

        databaseHelper = new DatabaseHelper(this);

        displayPatientCounts();
    }

    private void displayPatientCounts() {
        int criticalCount = databaseHelper.getPatientCountByStatus("Critical");
        int stableCount = databaseHelper.getPatientCountByStatus("Stable");
        int observationCount = databaseHelper.getPatientCountByStatus("Under Observation");

        textViewCriticalCount.setText("Critical Patients: " + criticalCount);
        textViewStableCount.setText("Stable Patients: " + stableCount);
        textViewObservationCount.setText("Under Observation Patients: " + observationCount);
    }
}
