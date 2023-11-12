package com.example.todo_timer;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;

import java.util.Optional;

/**
 * ToDo 타이머 애플리케이션의 컨트롤러 클래스
 */
public class TodoTimerController {

    @FXML
    private ListView<String> taskListView;

    @FXML
    private TextField taskInput;

    private final TaskManager taskManager;

    /**
     * TodoTimerController 생성자
     * TaskManager 인스턴스를 초기화
     */
    public TodoTimerController() {
        this.taskManager = new TaskManager();
    }

    /**
     * "추가" 버튼 클릭 시 호출되는 메서드
     * 입력된 작업을 작업 목록에 추가하고 입력 필드를 비움
     */
    @FXML
    private void addTask() {
        taskManager.addTask(taskListView, taskInput.getText());
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
        dialog.setHeaderText("수정할 작업:");
        dialog.setContentText("새로운 작업:");

        return dialog.showAndWait();
    }
}
