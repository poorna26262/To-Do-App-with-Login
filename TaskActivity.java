package com.example.todologin;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class TaskActivity extends Activity {
    private DatabaseHelper database;
    private SessionManager session;
    private long userId;
    private LinearLayout taskContainer;
    private TextView emptyState;
    private TextView welcomeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = new SessionManager(this);
        userId = session.getUserId();
        if (userId <= 0) {
            goToLogin();
            return;
        }
        setContentView(R.layout.activity_tasks);
        database = new DatabaseHelper(this);

        taskContainer = findViewById(R.id.taskContainer);
        emptyState = findViewById(R.id.emptyState);
        welcomeText = findViewById(R.id.welcomeText);
        Button addButton = findViewById(R.id.addTaskButton);
        Button logoutButton = findViewById(R.id.logoutButton);

        welcomeText.setText("Hello, " + database.getUserName(userId));
        addButton.setOnClickListener(v -> showAddTaskDialog());
        logoutButton.setOnClickListener(v -> {
            session.clearSession();
            goToLogin();
        });
        renderTasks();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (taskContainer != null && session != null && session.isLoggedIn()) renderTasks();
    }

    private void renderTasks() {
        List<TaskItem> tasks = database.getTasks(userId);
        taskContainer.removeAllViews();
        emptyState.setVisibility(tasks.isEmpty() ? View.VISIBLE : View.GONE);

        for (TaskItem task : tasks) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(12, 10, 8, 10);
            row.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);

            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(task.title);
            checkBox.setTextSize(16);
            checkBox.setChecked(task.completed);
            checkBox.setPaintFlags(task.completed
                    ? checkBox.getPaintFlags() | 0x10
                    : checkBox.getPaintFlags() & ~0x10);
            LinearLayout.LayoutParams checkParams =
                    new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            row.addView(checkBox, checkParams);

            Button deleteButton = new Button(this);
            deleteButton.setText("Delete");
            deleteButton.setTextAllCaps(false);
            row.addView(deleteButton);

            if (task.notes != null && !task.notes.trim().isEmpty()) {
                TextView notes = new TextView(this);
                notes.setText(task.notes);
                notes.setTextSize(13);
                notes.setPadding(44, 0, 8, 8);
                notes.setTextColor(0xFF667085);
                // Notes are placed in a vertical wrapper with the task row.
                LinearLayout wrapper = new LinearLayout(this);
                wrapper.setOrientation(LinearLayout.VERTICAL);
                wrapper.addView(row);
                wrapper.addView(notes);
                taskContainer.addView(wrapper);
            } else {
                taskContainer.addView(row);
            }

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                database.setTaskCompleted(task.id, userId, isChecked);
                renderTasks();
            });
            deleteButton.setOnClickListener(v -> new AlertDialog.Builder(this)
                    .setTitle("Delete task?")
                    .setMessage("This task will be permanently deleted.")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Delete", (dialog, which) -> {
                        database.deleteTask(task.id, userId);
                        renderTasks();
                    }).show());
        }
    }

    private void showAddTaskDialog() {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(24, 8, 24, 0);

        EditText titleInput = new EditText(this);
        titleInput.setHint("Task name");
        titleInput.setSingleLine(true);
        form.addView(titleInput);

        EditText notesInput = new EditText(this);
        notesInput.setHint("Notes (optional)");
        notesInput.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        notesInput.setMinLines(2);
        form.addView(notesInput);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Add a task")
                .setView(form)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String title = titleInput.getText().toString().trim();
                    String notes = notesInput.getText().toString().trim();
                    if (title.isEmpty()) {
                        titleInput.setError("Task name is required.");
                        return;
                    }
                    long id = database.addTask(userId, title, notes);
                    if (id > 0) {
                        dialog.dismiss();
                        renderTasks();
                    } else {
                        Toast.makeText(this, "Could not save task.", Toast.LENGTH_SHORT).show();
                    }
                }));
        dialog.show();
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (database != null) database.close();
    }
}
