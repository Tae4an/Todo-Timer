package com.example.todo_timer;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.Optional;
import java.util.ResourceBundle;


public class TodoTaskManageController implements Initializable {
    @FXML
    private TextField tskName;  // 사용자가 작업 이름을 입력할 수 있는 텍스트 필드

    @FXML
    private TextArea tskMemo;  // 작업에 대한 메모나 추가 정보를 입력할 수 있는 텍스트 영역

    @FXML
    private Button tsk_btn;  // "할 일" 또는 관련 기능을 수행하는 버튼

    @FXML
    private Button save_btn;  // 작업을 저장하는 기능을 수행하는 "저장" 버튼

    @FXML
    private Button delete_btn;  // 선택된 작업을 삭제하는 기능을 수행하는 "삭제" 버튼

    @FXML
    private DatePicker dueDatePicker;  // 작업의 마감일을 선택할 수 있는 날짜 선택기

    @FXML
    private AnchorPane tskManage_layout;  // 전체 작업 관리 UI 레이아웃을 담는 앵커 패인

    private final TodoTaskController todoTaskController;  // 작업 관리에 필요한 로직을 담당하는 컨트롤러 인스턴스

    private static TodoTaskManageController instance;  // TodoTaskManageController 클래스의 싱글톤 인스턴스

    private static String task;  // 현재 선택 또는 작업 중인 작업의 이름


    /**
     * TodoTaskManageController의 인스턴스를 반환하는 싱글톤 접근 메서드.
     *
     * @return TodoTaskManageController의 인스턴스
     */
    public static TodoTaskManageController getInstance() {
        // 인스턴스가 null인 경우 새로 생성
        if (instance == null) {
            instance = new TodoTaskManageController();
        }
        return instance;
    }

    /**
     * FXML 파일이 로드될 때 자동으로 호출되는 초기화 메서드.
     * UI 요소들의 초기 설정 및 이벤트 핸들러를 등록하는 역할을 수행.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 버튼에 이벤트 핸들러를 설정
        tsk_btn.setOnMouseClicked(event -> loadTodoTask()); // "할 일" 버튼 클릭 이벤트
        save_btn.setOnMouseClicked(event -> saveTask());    // "저장" 버튼 클릭 이벤트
        delete_btn.setOnMouseClicked(event -> deleteTask(task)); // "삭제" 버튼 클릭 이벤트

        // 선택된 작업 이름을 텍스트 필드에 설정
        tskName.setText(task);
        // 마감일 업데이트 메서드 호출
        updateDueDatePicker();

        tskMemo.setText(TodoTaskController.getInstance().getTaskMemo(task));

    }

    /**
     * TodoTaskManageController의 생성자.
     */
    public TodoTaskManageController() {
        // TodoTaskController 인스턴스 생성
        this.todoTaskController = new TodoTaskController();
    }

    /**
     * 선택된 작업을 설정하는 메서드.
     *
     * @param task 선택된 작업의 이름
     */
    public void setSelectTask(String task) {
        // tskName 필드가 null인 경우 새로운 TextField를 생성하여 할당함
        if (tskName == null) {
            this.tskName = new TextField();
        }
        // 전달받은 작업 이름을 현재 작업으로 설정함
        this.task = task;
    }

    public void loadTodoTask() {
        try {
            // TodoTask.fxml 파일을 로드하여 새로운 씬을 생성
            Parent todoTaskScene = FXMLLoader.load(getClass().getResource("TodoTask.fxml"));
            StackPane root = (StackPane) tskManage_layout.getScene().getRoot();



            // 현재 씬에 새로운 TodoTask 씬 추가
            root.getChildren().add(todoTaskScene);

            // 필요한 경우, 새 씬에 애니메이션 효과 적용
            todoTaskScene.setTranslateX(-340); // 씬의 너비에 맞게 조정
            Timeline timeline = new Timeline();
            KeyValue keyValue = new KeyValue(todoTaskScene.translateXProperty(), 0);
            KeyFrame keyFrame = new KeyFrame(Duration.millis(300), keyValue);
            timeline.getKeyFrames().add(keyFrame);
            timeline.play();

            // 이전 씬 제거
            root.getChildren().remove(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * 선택된 작업을 삭제하는 메서드.
     *
     * @param selectedTask 삭제할 작업의 이름
     */
    public void deleteTask(String selectedTask) {
        // 작업 삭제를 확인하는 다이얼로그를 생성 및 표시
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle("삭제 확인");
        alert.setHeaderText("다음 작업을 삭제하시겠습니까?\n\n" + selectedTask);



        // 사용자가 삭제를 확인하는 경우 해당 작업을 삭제함
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            todoTaskController.deleteTask(task);
            showPopup("삭제", "삭제 되었습니다..!");
            loadTodoTask();
        }
    }



    /**
     * 작업을 저장하는 메서드
     * 사용자가 수정한 작업 이름, 마감일 및 메모를 가져와서 저장
     * 작업 이름이 변경되고 중복된 경우 중복 메시지를 표시하고 함수를 종료
     * 작업 이름, 마감일 또는 메모가 변경된 경우 해당 변경 내용을 업데이트하고 저장
     * 변경 내용이 없는 경우 오류 메시지를 표시
     */
    private void saveTask() {
        String updatedTask = tskName.getText(); // 수정된 작업 이름 가져오기
        LocalDate dueDate = dueDatePicker.getValue(); // 수정된 마감일 가져오기
        String updatedMemo = tskMemo.getText(); // 수정된 메모 가져오기

        // 작업 이름이 변경되고 중복된 경우 처리
        if (!updatedTask.equals(task) && TodoTaskController.getInstance().isTaskNameExist(updatedTask)) {
            showPopup("중복된 작업", "이미 존재하는 작업 이름입니다.");
            return; // 중복된 경우 함수 종료
        }

        // 변경 여부 확인
        boolean isTaskNameChanged = !updatedTask.equals(task);
        boolean isDueDateChanged = dueDate != null && !dueDate.equals(TodoTaskController.getInstance().getDueDate(task));
        boolean isMemoChanged = !updatedMemo.equals(TodoTaskController.getInstance().getTaskMemo(task));

        // 변경된 내용이 있는 경우 처리
        if (isTaskNameChanged || isDueDateChanged || isMemoChanged) {
            if (isTaskNameChanged) {
                TodoTaskController.getInstance().updateTask(task, updatedTask);
                task = updatedTask; // 현재 작업 이름 업데이트
            }
            if (isDueDateChanged) {
                TodoTaskController.getInstance().updateDueDate(task, dueDate);
            }
            if (isMemoChanged) {
                TodoTaskController.getInstance().updateTaskMemo(task, updatedMemo);
            }

            showPopup("저장", "저장 되었습니다..!");
            loadTodoTask(); // 작업 목록 다시 불러오기
        } else {
            showPopup("Error", "변경된 내용이 없습니다..!");
        }
    }


    /**
     * 팝업 창을 표시하는 메서드.
     *
     * @param title   팝업 창 제목
     * @param message 팝업 메시지
     */
    private void showPopup(String title, String message) {
        // Platform.runLater() 메서드를 사용하여 JavaFX 애플리케이션 스레드에서 실행되도록 작업을 예약하는 데 사용
        // 이렇게 하면 showAndWait()가 다음 프레임에서 실행되므로 애니메이션이나 레이아웃 처리와 충돌하지 않음
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.getDialogPane().getStylesheets().add(getClass().getResource("/css/TodoTimer.css").toExternalForm());
            alert.showAndWait();
        });
    }

    /**
     * 데이트 피커의 Prompt Text를 마감일로 업데이트
     */
    private void updateDueDatePicker() {
        LocalDate dueDate = TodoTaskController.getInstance().getDueDate(task);

        dueDatePicker.setPromptText(dueDate != null ? dueDate.toString() : "마감일을 선택하세요..");
    }
}