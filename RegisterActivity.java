package com.example.todologin;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.security.GeneralSecurityException;

public class RegisterActivity extends Activity {
    private DatabaseHelper database;
    private EditText nameInput, emailInput, passwordInput, confirmPasswordInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        database = new DatabaseHelper(this);

        nameInput = findViewById(R.id.registerName);
        emailInput = findViewById(R.id.registerEmail);
        passwordInput = findViewById(R.id.registerPassword);
        confirmPasswordInput = findViewById(R.id.registerConfirmPassword);

        Button registerButton = findViewById(R.id.registerButton);
        TextView loginLink = findViewById(R.id.goToLogin);

        registerButton.setOnClickListener(v -> register());
        loginLink.setOnClickListener(v -> finish());
    }

    private void register() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();
        String confirm = confirmPasswordInput.getText().toString();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(this, "Please complete all fields.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Enter a valid email address.");
            return;
        }
        if (password.length() < 8) {
            passwordInput.setError("Use at least 8 characters.");
            return;
        }
        if (!password.equals(confirm)) {
            confirmPasswordInput.setError("Passwords do not match.");
            return;
        }

        try {
            long id = database.registerUser(name, email, password);
            if (id > 0) {
                Toast.makeText(this, "Account created. Please log in.", Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(this, "Registration failed.", Toast.LENGTH_SHORT).show();
            }
        } catch (SQLiteConstraintException e) {
            emailInput.setError("An account with this email already exists.");
        } catch (GeneralSecurityException e) {
            Toast.makeText(this, "Could not secure the password.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (database != null) database.close();
    }
}
