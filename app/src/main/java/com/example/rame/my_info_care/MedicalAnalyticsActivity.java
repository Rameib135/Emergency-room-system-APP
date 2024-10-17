package com.example.rame.my_info_care;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import java.io.File;
import java.util.ArrayList;

public class MedicalAnalyticsActivity extends AppCompatActivity {
    private DatabaseHelper databaseHelper;
    private TextView textViewCriticalCount, textViewStableCount, textViewObservationCount;
    private BarChart statusBarChart;
    private PieChart roomStatusPieChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_analytics);

        textViewCriticalCount = findViewById(R.id.textViewCriticalCount);
        textViewStableCount = findViewById(R.id.textViewStableCount);
        textViewObservationCount = findViewById(R.id.textViewObservationCount);
        statusBarChart = findViewById(R.id.statusBarChart);
        roomStatusPieChart = findViewById(R.id.roomStatusPieChart);

        databaseHelper = new DatabaseHelper(this);

        // Request storage permission on app launch
        requestStoragePermission();

        // Display the patient count information on the screen
        displayPatientCounts();

        // Set up the bar chart and pie chart
        setupStatusBarChart();
        setupRoomStatusPieChart();
    }

    // Function to request storage permission
    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    // Handle the result of permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied. Unable to save the PDF.", Toast.LENGTH_LONG).show();
            }
        }
    }

    // Function to display the patient counts in the TextViews
    private void displayPatientCounts() {
        int criticalCount = databaseHelper.getPatientCountByStatus("Critical");
        int stableCount = databaseHelper.getPatientCountByStatus("Stable");
        int observationCount = databaseHelper.getPatientCountByStatus("Under Observation");

        textViewCriticalCount.setText("Critical Patients: " + criticalCount);
        textViewStableCount.setText("Stable Patients: " + stableCount);
        textViewObservationCount.setText("Under Observation Patients: " + observationCount);
    }

    // Set up BarChart for patient status
    private void setupStatusBarChart() {
        ArrayList<BarEntry> entries = new ArrayList<>();

        // Add the patient counts to the BarEntries with index positions
        entries.add(new BarEntry(0, databaseHelper.getPatientCountByStatus("Critical")));  // Critical (Red)
        entries.add(new BarEntry(1, databaseHelper.getPatientCountByStatus("Stable")));    // Stable (Green)
        entries.add(new BarEntry(2, databaseHelper.getPatientCountByStatus("Under Observation")));  // Under Observation (Yellow)

        BarDataSet dataSet = new BarDataSet(entries, "Patient Status");

        // Apply the correct colors to each bar
        dataSet.setColors(new int[] {
                ContextCompat.getColor(this, R.color.critical_color),    // Red for Critical
                ContextCompat.getColor(this, R.color.stable_color),      // Green for Stable
                ContextCompat.getColor(this, R.color.under_observation_color)  // Yellow for Under Observation
        });

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.9f);  // Set custom bar width
        statusBarChart.setData(barData);
        statusBarChart.setFitBars(true);  // Make the bars fit nicely in the chart view
        statusBarChart.invalidate();  // Refresh the chart
    }

    // Set up PieChart for room status (In Hospital vs. Discharged)
    private void setupRoomStatusPieChart() {
        ArrayList<PieEntry> entries = new ArrayList<>();
        int inHospitalCount = databaseHelper.getRoomCount("In Hospital");
        int dischargedCount = databaseHelper.getRoomCount("Discharged");

        entries.add(new PieEntry(inHospitalCount, "In Hospital"));
        entries.add(new PieEntry(dischargedCount, "Discharged"));

        PieDataSet dataSet = new PieDataSet(entries, "Room Status");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        PieData data = new PieData(dataSet);
        roomStatusPieChart.setData(data);
        roomStatusPieChart.invalidate(); // Refresh chart
    }

    // Function to generate PDF report of the analytics
    public void generatePdfReport() {
        try {
            // Create a directory in external storage for saving the PDF
            File pdfDir = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Reports");
            if (!pdfDir.exists()) {
                pdfDir.mkdir();  // Create the directory if it doesn't exist
            }

            // Create a PDF file in the directory
            File file = new File(pdfDir, "MedicalReport.pdf");
            PdfWriter writer = new PdfWriter(file);
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);

            // Add title and analytics data to the PDF
            document.add(new Paragraph("Medical Analytics Report"));
            document.add(new Paragraph("Critical Patients: " + databaseHelper.getPatientCountByStatus("Critical")));
            document.add(new Paragraph("Stable Patients: " + databaseHelper.getPatientCountByStatus("Stable")));
            document.add(new Paragraph("Patients Under Observation: " + databaseHelper.getPatientCountByStatus("Under Observation")));
            document.add(new Paragraph("In Hospital: " + databaseHelper.getRoomCount("In Hospital")));
            document.add(new Paragraph("Discharged: " + databaseHelper.getRoomCount("Discharged")));

            // Close the document
            document.close();

            Toast.makeText(this, "PDF generated at " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error generating PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Function to handle the button click for generating the PDF
    public void onGeneratePdfButtonClick(View view) {
        generatePdfReport();  // This calls the function that generates the PDF
    }
}
