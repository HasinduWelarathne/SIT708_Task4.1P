package com.example.taskmanagerapp;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TaskAdapter.TaskActionListener {

    private RecyclerView recyclerView;
    private TextView textViewTotalTasks;
    private ImageButton btnAddTask;
    private static List<Task> taskList;
    private TaskAdapter taskAdapter;
    private DatabaseHelper databaseHelper;

    public static List<Task> getTaskList() {
        return taskList;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewTotalTasks = findViewById(R.id.textViewTotalTasks);
        btnAddTask = findViewById(R.id.btnAddTask);

        databaseHelper = new DatabaseHelper(this);

        recyclerView = findViewById(R.id.recyclerViewTasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        taskList = new ArrayList<>();

        loadTasks();


        taskAdapter = new TaskAdapter(taskList, this);
        recyclerView.setAdapter(taskAdapter);

        updateTotalTasksCount();

        btnAddTask.setOnClickListener(v -> showAddEditTaskDialog(null));
    }

    @Override
    public void onDeleteTask(Task task) {
        deleteTask(task);
    }

    @Override
    public void onEditTask(Task task) {
        showAddEditTaskDialog(task);
    }

    private void loadTasks() {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_TASKS, null, null, null, null, null, null);

        taskList.clear();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TITLE));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPTION));
                String dueDate = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DUE_DATE));

                Task task = new Task(id, title, description, dueDate);
                taskList.add(task);
            } while (cursor.moveToNext());

            cursor.close();
        }

        db.close();
    }

    private void showAddEditTaskDialog(@Nullable Task task) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_task, null);
        dialogBuilder.setView(dialogView);

        EditText editTextTitle = dialogView.findViewById(R.id.editTextTitle);
        EditText editTextDescription = dialogView.findViewById(R.id.editTextDescription);
        EditText editTextDueDate = dialogView.findViewById(R.id.editTextDueDate);
        Button btnSave = dialogView.findViewById(R.id.btnSave);

        if (task != null) {
            editTextTitle.setText(task.getTitle());
            editTextDescription.setText(task.getDescription());
            editTextDueDate.setText(task.getDueDate());
        }

        AlertDialog alertDialog = dialogBuilder.create();

        btnSave.setOnClickListener(v -> {
            String title = editTextTitle.getText().toString().trim();
            String description = editTextDescription.getText().toString().trim();
            String dueDate = editTextDueDate.getText().toString().trim();

            if (title.isEmpty() || dueDate.isEmpty()) {
                Toast.makeText(MainActivity.this, "Title and Due Date are required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isValidDate(dueDate)) {
                Toast.makeText(MainActivity.this, "Invalid Date Format. Use MM/dd/yyyy", Toast.LENGTH_SHORT).show();
                return;
            }

            if (task == null) {
                // Add new task
                Task newTask = new Task(0, title, description, dueDate);
                addTask(newTask);
            } else {
                // Update existing task
                task.setTitle(title);
                task.setDescription(description);
                task.setDueDate(dueDate);
                updateTask(task);
            }

            alertDialog.dismiss();
        });

        alertDialog.show();
    }

    private void addTask(Task task) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_TITLE, task.getTitle());
        values.put(DatabaseHelper.COLUMN_DESCRIPTION, task.getDescription());
        values.put(DatabaseHelper.COLUMN_DUE_DATE, task.getDueDate());

        long id = db.insert(DatabaseHelper.TABLE_TASKS, null, values);
        task.setId((int) id);

        db.close();

        taskList.add(task);
        taskAdapter.notifyDataSetChanged();

        updateTotalTasksCount();
    }

    private void updateTask(Task task) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_TITLE, task.getTitle());
        values.put(DatabaseHelper.COLUMN_DESCRIPTION, task.getDescription());
        values.put(DatabaseHelper.COLUMN_DUE_DATE, task.getDueDate());

        db.update(DatabaseHelper.TABLE_TASKS, values, DatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(task.getId())});

        db.close();

        int index = taskList.indexOf(task);
        if (index != -1) {
            taskList.set(index, task);
            taskAdapter.notifyItemChanged(index);
        }

        updateTotalTasksCount();
    }

    private void deleteTask(Task task) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        db.delete(DatabaseHelper.TABLE_TASKS, DatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(task.getId())});

        db.close();

        taskList.remove(task);
        taskAdapter.notifyDataSetChanged();

        updateTotalTasksCount();
    }

    private void updateTotalTasksCount() {
        int totalTasksCount = taskList.size();
        textViewTotalTasks.setText(String.valueOf(totalTasksCount));
    }

    private boolean isValidDate(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        sdf.setLenient(false);

        try {
            Date date = sdf.parse(dateStr);
            if (date != null) {
                // Date is valid and matches the format
                return true;
            } else {
                // Date is null, which means the date string is not in the correct format
                return false;
            }
        } catch (ParseException e) {
            // Date is not valid
            return false;
        }
    }

}
