package com.example.rame.my_info_care;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private Button buttonProceed;
    private String userType, userIdentity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonProceed = findViewById(R.id.buttonProceed);

        userType = getIntent().getStringExtra("USER_TYPE");
        userIdentity = getIntent().getStringExtra("USER_IDENTITY");

        Log.d("MainActivity", "User type: " + userType); // Debugging
        Log.d("MainActivity", "User identity: " + userIdentity); // Debugging

        buttonProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MainActivity", "Button clicked, userType: " + userType); // Debugging
                if (userType != null && userType.equalsIgnoreCase("Doctor")) {
                    Log.d("MainActivity", "Navigating to MedicalStaffActivity"); // Debugging
                    Intent intent = new Intent(MainActivity.this, MedicalStaffActivity.class);
                    startActivity(intent);
                } else {
                    Log.d("MainActivity", "Navigating to PatientActivity"); // Debugging
                    Intent intent = new Intent(MainActivity.this, PatientActivity.class);
                    intent.putExtra("USER_IDENTITY", userIdentity);
                    startActivity(intent);
                }
            }
        });
    }
}
