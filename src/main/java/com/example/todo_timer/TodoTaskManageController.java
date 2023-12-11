package com.example.todo_timer;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class TodoTaskManageController implements Initializable {
    @FXML
    private TextField tskName;
    @FXML
    private TextArea tskMemo;
    @FXML
    private Button tsk_btn;
    @FXML
    private Button save_btn;

    @FXML
    private AnchorPane tskManage_layout;

    private static TodoTaskManageController instance;
    private static String task;

    public static TodoTaskManageController getInstance() {
        if (instance == null) {
            instance = new TodoTaskManageController();
        }
        return instance;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tsk_btn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                loadTodoTask();
            }
        });
        save_btn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {

                // tskName에서 작업 이름을 가져오고, tasks 목록을 업데이트합니다.
                String updatedTask = tskName.getText();
                if (updatedTask != null && !updatedTask.isEmpty() && !updatedTask.equals(task)) {
                    TodoTaskController.getInstance().updateTask(task, updatedTask);
                    showPopup("저장","저장 되었습니다..!");
                    loadTodoTask();
                    // 필요에 따라 다른 UI 업데이트 또는 작업 수행
                }

            }

        });
        tskName.setText(task);
    }

    public TodoTaskManageController() {
    }

    public void setSelectTask(String task) {
        if (tskName == null) {
            this.tskName = new TextField();
        }
        this.task = task;
    }

    public void loadTodoTask(){
        StackPane stackPane = (StackPane) tskManage_layout.getScene().getRoot();
        Parent sub = (Parent) stackPane.getChildren().get(1);

        Timeline timeline = new Timeline();
        KeyValue keyValue = new KeyValue(sub.translateXProperty(), 400);
        KeyFrame keyFrame = new KeyFrame(Duration.millis(300), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                stackPane.getChildren().remove(1);
            }
        }, keyValue);
        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
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

}

