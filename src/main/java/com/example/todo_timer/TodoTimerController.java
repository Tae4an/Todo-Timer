package com.example.todo_timer;
// TodoTimerController.java
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;

import java.util.Optional;

public class TodoTimerController {

    @FXML
    private ListView<String> taskListView;

    @FXML
    private TextField taskInput;

    private final TaskManager taskManager;

    public TodoTimerController() {
        this.taskManager = new TaskManager();
    }

    @FXML
    private void addTask() {
        taskManager.addTask(taskListView, taskInput.getText());
        taskInput.clear();
    }

    @FXML
    private void editTask() {
        int selectedIndex = taskListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            String currentTask = taskListView.getItems().get(selectedIndex);
            Optional<String> result = showEditDialog(currentTask);

            result.ifPresent(updatedTask -> taskManager.editTask(taskListView, selectedIndex, updatedTask));
        }
    }

    @FXML
    private void deleteTask() {
        int selectedIndex = taskListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            taskManager.deleteTask(taskListView, selectedIndex);
        }
    }

    private Optional<String> showEditDialog(String currentTask) {
        TextInputDialog dialog = new TextInputDialog(currentTask);
        dialog.setTitle("작업 수정");
        dialog.setHeaderText("수정할 작업:");
        dialog.setContentText("새로운 작업:");

        return dialog.showAndWait();
    }
}
