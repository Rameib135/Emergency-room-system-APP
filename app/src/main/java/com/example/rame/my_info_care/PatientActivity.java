package com.example.rame.my_info_care;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.graphics.Color;
import java.util.ArrayList;
import java.util.List;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.formatter.PercentFormatter;  // For percent formatting
import java.util.Locale;  // For locale-specific formatting
import com.github.mikephil.charting.components.Description;



public class PatientActivity extends AppCompatActivity {
    private TextView textViewStatus, textViewRemarks, textViewDischargeStatus;
    private DatabaseHelper databaseHelper;
    private ListView listViewStatusHistory;
    private String userIdentity;
    private PieChart pieChart;  // Add a PieChart

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient);

        // Initialize views
        textViewStatus = findViewById(R.id.textViewStatus);
        textViewRemarks = findViewById(R.id.textViewRemarks);
        textViewDischargeStatus = findViewById(R.id.textViewDischargeStatus);
        listViewStatusHistory = findViewById(R.id.listViewStatusHistory);
        pieChart = findViewById(R.id.pieChart);  // Initialize the chart

        databaseHelper = new DatabaseHelper(this);
        userIdentity = getIntent().getStringExtra("USER_IDENTITY");

        loadPatientStatus();
        loadStatusHistory();
        loadOccupancyData();  // Call the method to load data into the chart

    }

    private void loadPatientStatus() {
        Cursor cursor = databaseHelper.getPatientStatus(userIdentity);
        if (cursor != null && cursor.moveToFirst()) {
            int statusIndex = cursor.getColumnIndex("status");
            int remarksIndex = cursor.getColumnIndex("remarks");
            int dischargeIndex = cursor.getColumnIndex("discharge_status");

            if (statusIndex != -1 && remarksIndex != -1 && dischargeIndex != -1) {
                String status = cursor.getString(statusIndex);
                String remarks = cursor.getString(remarksIndex);
                String dischargeStatus = cursor.getString(dischargeIndex);

                textViewStatus.setText(getString(R.string.status_text, status));
                textViewRemarks.setText(getString(R.string.remarks_text, remarks));
                textViewDischargeStatus.setText("Discharge Status: " + dischargeStatus);
            }
            cursor.close();
        } else {
            textViewStatus.setText(getString(R.string.status_text, "Not available"));
            textViewRemarks.setText(getString(R.string.remarks_text, "Not available"));
            textViewDischargeStatus.setText("Discharge Status: Not available");
        }
    }

    private void loadStatusHistory() {
        int patientId = getPatientIdByIdentity(userIdentity);
        List<String> statusUpdates = getStatusHistory(patientId);

        // Set the status history to the ListView
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
                    statusUpdates.add("Status: " + status + "\nRemarks: " + remarks + "\nTime: " + timestamp);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return statusUpdates;
    }

    @SuppressLint("Range")
    private int getPatientIdByIdentity(String identityNumber) {
        Cursor cursor = databaseHelper.getPatientIdByIdentity(identityNumber);
        int patientId = -1;
        if (cursor != null && cursor.moveToFirst()) {
            patientId = cursor.getInt(cursor.getColumnIndex("id"));
            cursor.close();
        }
        return patientId;
    }

    // Load room occupancy data into the PieChart
    private void loadOccupancyData() {
        // Total rooms available in the hospital
        int totalRooms = 4;

        // Fetch the number of patients currently in the hospital
        int occupiedRooms = databaseHelper.getRoomCount("In Hospital");

        // Calculate available rooms
        int availableRooms = totalRooms - occupiedRooms;

        // Prepare data for the pie chart
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(occupiedRooms, "In Hospital"));
        entries.add(new PieEntry(availableRooms, "Available"));

        // Set data and customize the appearance of the pie chart
        PieDataSet dataSet = new PieDataSet(entries, "Room Occupancy");
        dataSet.setColors(new int[]{Color.GREEN, Color.GRAY}); // Occupied in green, available in gray
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);

        PieData data = new PieData(dataSet);

        // Set custom formatter to show percentage
        data.setValueFormatter(new PercentFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format(Locale.getDefault(), "%.1f%%", value);  // Show one decimal point and % symbol
            }
        });

        pieChart.setData(data);

        // Customization
        pieChart.setDrawHoleEnabled(false);
        pieChart.setUsePercentValues(true); // Show percentages
        pieChart.getDescription().setEnabled(false); // Hide description
        pieChart.invalidate(); // Refresh the chart
    }



}
