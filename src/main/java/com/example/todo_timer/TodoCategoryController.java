package com.example.todo_timer;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class TodoCategoryController implements Initializable {
    @FXML
    private Button tm_btn; // "타이머" 기능을 위한 버튼

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // "타이머" 버튼에 대한 클릭 이벤트 핸들러 설정
        tm_btn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                // TodoTimer.fxml 뷰를 로드하고 화면에 추가
                try {
                    Parent sub = FXMLLoader.load(getClass().getResource("TodoTimer.fxml"));
                    StackPane root = (StackPane) tm_btn.getScene().getRoot();
                    root.getChildren().add(sub);

                    // 뷰에 애니메이션 효과 적용
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
    }
}
