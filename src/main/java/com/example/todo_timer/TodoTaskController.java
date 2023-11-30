package com.example.todo_timer;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class TodoTaskController implements Initializable {
    public AnchorPane task_layout;
    @FXML
    private ListView<String> taskListView;  // 작업 목록을 나타내는 ListView. 사용자가 작업을 추가, 수정, 삭제할 때 업데이트 됨

    @FXML
    private TextField taskInput;  // 사용자가 새 작업을 입력하는 TextField. "추가" 버튼을 클릭하면 작업 목록에 새로운 작업이 추가
    private final TaskManager taskManager;  // 작업 관리자(TaskManager) 인스턴스. 작업 목록을 관리하는 데 사용됨

    @FXML
    private Button tm_btn;
    @FXML
    private AnchorPane timer_layout;



    public TodoTaskController() {
        this.taskManager = new TaskManager();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tm_btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    // TodoTimer.fxml을 불러와서 root에 추가
                    Parent todoTimer = FXMLLoader.load(getClass().getResource("TodoTimer.fxml"));
                    AnchorPane root = (AnchorPane) tm_btn.getScene().getRoot();

                    // 현재 화면의 모든 자식 노드를 제거
                    root.getChildren().clear();

                    // TodoTimer.fxml 추가
                    root.getChildren().add(todoTimer);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
    /**
     * "추가" 버튼 클릭 시 호출되는 메서드
     * 입력된 작업을 작업 목록에 추가하고, 입력 필드를 비움
     */
    @FXML
    private void addTask() {
        // TaskManager를 사용하여 작업 목록에 새로운 작업을 추가
        taskManager.addTask(taskListView, taskInput.getText());
        // 작업 추가 후, 입력 필드를 비워 사용자에게 새 작업을 입력할 수 있도록 함
        taskInput.clear();
    }

    /**
     * "수정" 버튼 클릭 시 호출되는 메서드
     * 선택된 작업을 수정하도록 다이얼로그를 표시하고,
     * 수정된 작업을 TaskManager를 통해 업데이트
     */
    @FXML
    private void editTask() {
        // 선택된 작업의 인덱스를 가져옴
        int selectedIndex = taskListView.getSelectionModel().getSelectedIndex();

        // 선택된 작업이 있는 경우에만 수정 다이얼로그를 표시
        if (selectedIndex >= 0) {
            // 선택된 작업의 현재 내용을 가져옴
            String currentTask = taskListView.getItems().get(selectedIndex);

            // 수정 다이얼로그를 표시하고, 사용자가 입력한 내용을 받음
            Optional<String> result = showEditDialog(currentTask);

            // 사용자가 내용을 입력한 경우에만 TaskManager를 통해 작업을 수정
            result.ifPresent(updatedTask -> taskManager.editTask(taskListView, selectedIndex, updatedTask));
        }
    }

    /**
     * "삭제" 버튼 클릭 시 호출되는 메서드
     * 선택된 작업을 TaskManager를 통해 삭제
     */
    @FXML
    private void deleteTask() {
        // 선택된 작업의 인덱스를 가져옴
        int selectedIndex = taskListView.getSelectionModel().getSelectedIndex();

        // 선택된 작업이 있는 경우에만 TaskManager를 통해 해당 작업을 삭제
        if (selectedIndex >= 0) {
            taskManager.deleteTask(taskListView, selectedIndex);
        }
    }

    /**
     * 작업을 수정하기 위한 다이얼로그를 표시하는 메서드
     *
     * @param currentTask 수정할 작업의 현재 내용
     * @return 사용자가 입력한 새로운 작업 내용(Optional).
     */
    private Optional<String> showEditDialog(String currentTask) {
        TextInputDialog dialog = new TextInputDialog(currentTask);
        dialog.setTitle("작업 수정");
        dialog.setHeaderText("수정할 작업");
        dialog.setContentText("새로운 작업:");

        return dialog.showAndWait();
    }

}
