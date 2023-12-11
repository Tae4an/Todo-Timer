package com.example.todo_timer;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class TodoTaskController implements Initializable {
    @FXML
    private ChoiceBox<String> taskChoiceBox;  // 작업을 선택할 ChoiceBox

    @FXML
    private ListView<String> taskListView;  // 작업 목록을 나타내는 ListView. 사용자가 작업을 추가, 수정, 삭제할 때 업데이트 됨

    @FXML
    private Button tm_btn;  //타이머 홈 버튼

    @FXML
    private Button tskmanage_btn;  //작업관리 홈 홈 버튼

    // 데이터의 변경 사항을 감지하고 자동으로 UI에 반영할 수 있도록 도와주는 컬렉션
    private static final ObservableList<String> tasks = FXCollections.observableArrayList();

    private TodoTaskManageController manageController;  // 이 부분을 추가


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
        tm_btn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                try {
                    Parent sub = FXMLLoader.load(getClass().getResource("TodoTimer.fxml"));
                    StackPane root = (StackPane) tm_btn.getScene().getRoot();
                    root.getChildren().add(sub);

                    sub.setTranslateY(600);

                    Timeline timeline = new Timeline();
                    KeyValue keyValue = new KeyValue(sub.translateYProperty(), 0);
                    KeyFrame keyFrame = new KeyFrame(Duration.millis(300), keyValue);
                    timeline.getKeyFrames().add(keyFrame);
                    timeline.play();

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        tskmanage_btn.setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                // 선택한 작업을 얻어옴
                String selectedTask = taskListView.getSelectionModel().getSelectedItem();

                if (taskListView.getItems().isEmpty()) {
                    showPopup("Error","작업이 없습니다..!");
                }
                else if (selectedTask == null){
                    showPopup("Error","작업을 선택하세요..!");
                }

                else {
                    manageController = TodoTaskManageController.getInstance();
                    manageController.setSelectTask(selectedTask);
                    try {
                        Parent sub = FXMLLoader.load(getClass().getResource("TodoTaskManage.fxml"));
                        StackPane root = (StackPane) tskmanage_btn.getScene().getRoot();
                        root.getChildren().add(sub);

                        sub.setTranslateX(500);

                        Timeline timeline = new Timeline();
                        KeyValue keyValue = new KeyValue(sub.translateXProperty(), 0);
                        KeyFrame keyFrame = new KeyFrame(Duration.millis(300), keyValue);
                        timeline.getKeyFrames().add(keyFrame);
                        timeline.play();

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                }
            }
        });
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

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(taskName -> {
            if (!taskName.isEmpty()) {
                tasks.add(taskName);
                updateTaskList();  // ListView 업데이트
            }
        });
    }

    /**
     * 선택한 작업을 삭제하기 위한 메서드
     *
     * @param selectedTask 삭제할 작업
     */
    public void deleteTask(String selectedTask) {
         tasks.remove(selectedTask);
         updateTaskList();
    }

    /**
     * 작업을 업데이트하는 메서드
     * @param oldTask 이전 작업 이름
     * @param newTask 새로운 작업 이름
     */
    public void updateTask(String oldTask, String newTask) {
        tasks.remove(oldTask);
        tasks.add(newTask);
        updateTaskList();
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
        // taskListView가 null이면 초기화
        if (taskListView == null) {
            taskListView = new ListView<>();
        }

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