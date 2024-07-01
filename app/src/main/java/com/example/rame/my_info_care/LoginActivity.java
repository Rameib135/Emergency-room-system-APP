package com.example.rame.my_info_care;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private EditText editTextEmail, editTextPassword;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        databaseHelper = new DatabaseHelper(this);
    }

    public void loginUser(View view) {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (databaseHelper.checkUser(email, password)) {
            String userType = databaseHelper.getUserType(email);
            String userIdentity = databaseHelper.getUserIdentity(email);
            Log.d("LoginActivity", "User type: " + userType); // Debugging
            Log.d("LoginActivity", "User identity: " + userIdentity); // Debugging
            if (userType != null) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("USER_TYPE", userType);
                intent.putExtra("USER_IDENTITY", userIdentity);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "User type not found", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
        }
    }

    public void goToRegister(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }
}
