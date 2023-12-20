package com.example.todo_timer;

import javafx.animation.FadeTransition;
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
import javafx.scene.text.Font;
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

    private static String task;  // 현재 선택 또는 작업 중인 작업의 이름


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

        tskMemo.setText(todoTaskController.getTaskMemo(todoTaskController.getCurrentProjectName(),task));


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

            // 새 씬에 페이드 인 애니메이션 적용
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), todoTaskScene);
            fadeIn.setFromValue(0); // 시작 투명도를 0으로 설정 (완전히 투명)
            fadeIn.setToValue(1); // 종료 투명도를 1로 설정 (완전히 불투명)

            fadeIn.play(); // 애니메이션 실행
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
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION); // 타입을 CONFIRMATION으로 설정
        alert.setTitle("삭제 확인");
        alert.setHeaderText("다음 작업을 삭제하시겠습니까?\n\n" + selectedTask);

        ButtonType deleteButton = new ButtonType("삭제");
        ButtonType cancelButton = new ButtonType("취소", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(deleteButton, cancelButton);

        // 다이얼로그 패널에 접근 >> 신창영
        DialogPane dialogPane = alert.getDialogPane();

        dialogPane.getStylesheets().add(getClass().getResource("/css/TodoTimerManage.css").toExternalForm());
        dialogPane.getStyleClass().add("custom-dialog");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == deleteButton) {
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
        if (!updatedTask.equals(task) && todoTaskController.isTaskNameExist(updatedTask)) {
            showPopup("중복된 작업", "이미 존재하는 작업 이름입니다.");
            return; // 중복된 경우 함수 종료
        }

        // 변경 여부 확인
        boolean isTaskNameChanged = !updatedTask.equals(task);
        boolean isDueDateChanged = dueDate != null && !dueDate.equals(todoTaskController.getDueDate(todoTaskController.getCurrentProjectName(),task));
        boolean isMemoChanged = !updatedMemo.equals(todoTaskController.getTaskMemo(todoTaskController.getCurrentProjectName(), task));

        // 변경된 내용이 있는 경우 처리
        if (isTaskNameChanged || isDueDateChanged || isMemoChanged) {
            if (isTaskNameChanged) {
                todoTaskController.updateTask(task, updatedTask);
                task = updatedTask; // 현재 작업 이름 업데이트
            }
            if (isDueDateChanged) {
                todoTaskController.updateDueDate(todoTaskController.getCurrentProjectName(),task, dueDate);
            }
            if (isMemoChanged) {
                todoTaskController.updateTaskMemo(todoTaskController.getCurrentProjectName(),task, updatedMemo);
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

    private void updateDueDatePicker() {
        LocalDate dueDate = todoTaskController.getDueDate(todoTaskController.getCurrentProjectName(),task);
        dueDatePicker.setValue(dueDate); // 기존 마감일을 설정

        // 현재 날짜 이전의 모든 날짜를 비활성화
        dueDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate today = LocalDate.now();
                setDisable(empty || date.compareTo(today) < 0); // 오늘 날짜 이전은 선택 불가능
            }
        });
        // Prompt Text 설정
        dueDatePicker.setPromptText(dueDate != null ? dueDate.toString() : "마감일을 선택하세요..");
    }




}