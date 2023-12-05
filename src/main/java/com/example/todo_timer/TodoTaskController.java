package com.example.todo_timer;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class TodoTaskController implements Initializable {
    @FXML
    private ListView<String> taskListView;  // 작업 리스트 뷰

    @FXML
    private Button tm_btn;  //타이머 홈 버튼
    public AnchorPane task_layout;  // 작업 홈의 레이아웃

    // 데이터의 변경 사항을 감지하고 자동으로 UI에 반영할 수 있도록 도와주는 컬렉션
    private static final ObservableList<String> tasks = FXCollections.observableArrayList();

    // TodoTaskController 클래스의 인스턴스를 싱글톤 패턴으로 관리하기 위한 변수
    private static TodoTaskController instance;

    /**
     * Singleton 패턴을 사용하여 TodoTaskController의 인스턴스를 반환
     *
     * @return TodoTaskController의 인스턴스
     */
    public static TodoTaskController getInstance() {
        if (instance == null) {
            instance = new TodoTaskController();
        }
        return instance;
    }

    /**
     * TodoTaskController의 생성자
     */
    public TodoTaskController() {
    }


    /**
     * FXML 파일이 로드될 때 자동으로 호출되는 초기화 메서드
     * UI 요소들의 초기 설정 및 이벤트 핸들러를 등록하는 역할을 수행
     *
     * @param location  FXML 파일의 위치
     * @param resources 리소스 번들
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // "작업 관리" 버튼에 대한 액션 설정
        tm_btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    // TodoTimer.fxml 파일을 로드하여 Parent 객체 생성
                    Parent todoTimer = FXMLLoader.load(getClass().getResource("TodoTimer.fxml"));

                    // 현재 Scene의 Root를 얻어옴
                    AnchorPane root = (AnchorPane) tm_btn.getScene().getRoot();

                    // Root의 자식 노드들을 모두 제거
                    root.getChildren().clear();

                    // 새로운 TodoTimer.fxml을 Root에 추가하여 화면 교체
                    root.getChildren().add(todoTimer);
                } catch (IOException e) {
                    // 예외 발생 시 런타임 예외로 처리
                    throw new RuntimeException(e);
                }
            }
        });
        // 작업 목록 업데이트
        updateTaskList();
    }

    /**
     * 작업을 추가하는 메서드
     */
    @FXML
    private void addTask() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("작업 추가");
        dialog.setHeaderText("새로운 작업을 추가하세요");
        dialog.setContentText("작업 이름:");

        // TextInputDialog를 화면에 표시하고, 사용자가 입력한 값을 기다림
        Optional<String> result = dialog.showAndWait();

        // Optional 객체에 값이 존재할 경우, 즉 사용자가 입력한 작업 이름이 비어 있지 않은 경우에 대한 처리
        result.ifPresent(taskName -> {
            // 작업 이름이 비어있지 않은 경우에만 실행
            if (!taskName.isEmpty()) {
                // tasks 리스트에 새로운 작업을 추가
                tasks.add(taskName);
                // 작업이 추가된 tasks 리스트를 ListView에 반영하여 화면을 업데이트
                taskListView.setItems(tasks);
            }
        });
    }

    /**
     * 선택한 작업을 삭제하기 위한 메서드
     *
     * @param selectedIndex 삭제할 작업의 인덱스
     */
    private void deleteTask(int selectedIndex) {
        // 선택한 작업의 이름을 얻어옴
        String selectedTask = tasks.get(selectedIndex);

        // 확인 다이얼로그를 생성
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("삭제 확인");
        alert.setHeaderText("다음 작업을 삭제하시겠습니까?\n\n" + selectedTask);

        // 사용자의 선택 결과를 얻어옴
        Optional<ButtonType> result = alert.showAndWait();

        // 사용자가 확인을 선택한 경우에만 작업을 삭제
        if (result.isPresent() && result.get() == ButtonType.OK) {
            tasks.remove(selectedIndex);
            taskListView.setItems(tasks);
        }
    }


    /**
     * 선택한 작업을 관리하기 위한 메서드
     * 선택한 작업에 대한 다이얼로그를 표시하고, 사용자가 선택한 작업을 수정하거나 삭제할 수 있는 기능을 제공
     * 수정 또는 삭제 작업은 사용자가 확인을 선택한 경우에만 수행
     */
    @FXML
    private void managementTask() {
        // 선택한 작업의 인덱스를 얻어옴
        int selectedIndex = taskListView.getSelectionModel().getSelectedIndex();

        // 유효한 인덱스인 경우에만 다이얼로그를 표시
        if (selectedIndex >= 0) {
            // 선택한 작업의 이름을 얻어옴
            String selectedTask = tasks.get(selectedIndex);

            // 작업 관리 다이얼로그를 생성
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("작업 관리");
            alert.setHeaderText("다음 작업을 어떻게 관리하시겠습니까?\n\n" + selectedTask);

            // 다이얼로그에 수정, 삭제, 취소 버튼을 추가하고, 각 버튼의 동작을 정의
            ButtonType editButton = new ButtonType("수정");
            ButtonType deleteButton = new ButtonType("삭제");
            ButtonType cancelButton = new ButtonType("취소", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(editButton, deleteButton, cancelButton);

            // 사용자의 선택 결과를 얻어옴
            Optional<ButtonType> result = alert.showAndWait();

            // 사용자가 선택한 작업을 수정 또는 삭제
            if (result.isPresent()) {
                if (result.get() == editButton) {
                    // 수정 버튼을 선택한 경우
                    showEditDialog(selectedTask).ifPresent(updatedTask -> {
                        tasks.set(selectedIndex, updatedTask);
                        taskListView.getItems().set(selectedIndex, updatedTask);
                    });
                } else if (result.get() == deleteButton) {
                    // 삭제 버튼을 선택한 경우
                    deleteTask(selectedIndex);
                }
            }
        } else {
            // 선택한 작업이 없을 경우
            showPopup("Error", "작업을 선택하세요..!");
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
            alert.showAndWait();
        });
    }


    /**
     * 수정 다이얼로그를 표시하고, 사용자가 입력한 수정된 작업 이름을 반환하는 메서드
     *
     * @param currentTask 현재 작업의 이름
     * @return 사용자가 입력한 수정된 작업 이름의 Optional 객체
     */
    private Optional<String> showEditDialog(String currentTask) {
        // 수정 다이얼로그 생성 및 초기값 설정
        TextInputDialog dialog = new TextInputDialog(currentTask);
        dialog.setTitle("작업 수정");
        dialog.setHeaderText("수정할 작업");
        dialog.setContentText("새로운 작업:");

        // 다이얼로그를 표시하고, 사용자가 입력한 값을 반환
        return dialog.showAndWait();
    }

    /**
     * 작업 목록을 업데이트하는 메서드
     * 작업 목록이 비어 있고 실제 작업이 존재하는 경우에만 목록을 업데이트
     */
    private void updateTaskList() {
        // 작업 목록이 비어 있고, 실제 작업이 존재하는 경우에만 목록을 업데이트
        if (taskListView.getItems().isEmpty() && !tasks.isEmpty()) {
            taskListView.setItems(tasks);
        }
    }

    /**
     * 작업 목록을 반환
     *
     * @return 작업 목록
     */
    public ObservableList<String> getTasks() {
        return tasks;
    }
}