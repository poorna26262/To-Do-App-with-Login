package com.example.todologin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.security.GeneralSecurityException;

public class LoginActivity extends Activity {
    private DatabaseHelper database;
    private SessionManager session;
    private EditText emailInput;
    private EditText passwordInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = new SessionManager(this);
        if (session.isLoggedIn()) {
            openTasks();
            return;
        }
        setContentView(R.layout.activity_login);
        database = new DatabaseHelper(this);
        emailInput = findViewById(R.id.loginEmail);
        passwordInput = findViewById(R.id.loginPassword);

        Button loginButton = findViewById(R.id.loginButton);
        TextView registerLink = findViewById(R.id.goToRegister);

        loginButton.setOnClickListener(v -> login());
        registerLink.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void login() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter email and password.", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            long userId = database.authenticate(email, password);
            if (userId > 0) {
                session.createSession(userId);
                openTasks();
            } else {
                Toast.makeText(this, "Invalid email or password.", Toast.LENGTH_SHORT).show();
            }
        } catch (GeneralSecurityException e) {
            Toast.makeText(this, "Unable to verify password on this device.", Toast.LENGTH_LONG).show();
        }
    }

    private void openTasks() {
        startActivity(new Intent(this, TaskActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (database != null) database.close();
    }
}
