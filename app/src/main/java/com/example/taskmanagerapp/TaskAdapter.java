package com.example.taskmanagerapp;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private static TaskActionListener taskActionListener;

    public TaskAdapter(List<Task> taskList, TaskActionListener taskActionListener) {
        this.taskList = taskList;
        this.taskActionListener = taskActionListener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_item, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.bind(task);
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public interface TaskActionListener {
        void onDeleteTask(Task task);
        void onEditTask(Task task);
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {

        private TextView textViewTaskNumber;
        private TextView textViewTaskTitle;
        private TextView textViewDueDate;
        private ImageView imageViewMoreOptions;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTaskNumber = itemView.findViewById(R.id.textViewTaskNumber);
            textViewTaskTitle = itemView.findViewById(R.id.textViewTaskTitle);
            textViewDueDate = itemView.findViewById(R.id.textViewDueDate);
            imageViewMoreOptions = itemView.findViewById(R.id.imageViewMoreOptions);
        }

        @SuppressLint("NonConstantResourceId")
        public void bind(Task task) {
            textViewTaskNumber.setText(String.valueOf(getAdapterPosition() + 1));
            textViewTaskTitle.setText(task.getTitle());
            textViewDueDate.setText("Due Date: " + task.getDueDate());

            imageViewMoreOptions.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
                popupMenu.inflate(R.menu.menu_more_options); // Create menu_more_options.xml
                popupMenu.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == R.id.menu_delete) {
                        taskActionListener.onDeleteTask(task);
                        return true;
                    }
                    return false;
                });
                popupMenu.show();
            });
            itemView.setOnClickListener(v -> {
                taskActionListener.onEditTask(task);
            });
        }
    }
}
