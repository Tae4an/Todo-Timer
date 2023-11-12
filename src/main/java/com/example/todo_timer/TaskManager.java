package com.example.todo_timer;

import javafx.collections.ObservableList;
import javafx.scene.control.ListView;

public class TaskManager {

    public void addTask(ListView<String> taskListView, String task) {
        if (!task.isEmpty()) {
            taskListView.getItems().add(task);
        }
    }

    public void editTask(ListView<String> taskListView, int index, String updatedTask) {
        if (!updatedTask.isEmpty()) {
            ObservableList<String> tasks = taskListView.getItems();
            tasks.set(index, updatedTask);
        }
    }

    public void deleteTask(ListView<String> taskListView, int index) {
        taskListView.getItems().remove(index);
    }
}
