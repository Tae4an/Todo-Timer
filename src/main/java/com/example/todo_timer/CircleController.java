package com.example.todo_timer;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;


//임시적으로 Circle.fxml를 돌리는 코드
public class CircleController {

    @FXML
    private Circle donutCircle;

    private Timeline timeline;

    @FXML
    private void initialize() {
        // 애니메이션 타임라인 생성
        timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(donutCircle.strokeProperty(), Color.SKYBLUE)),
                new KeyFrame(Duration.seconds(1), new KeyValue(donutCircle.strokeProperty(), Color.web("#CEF6F5"))),
                new KeyFrame(Duration.seconds(2), new KeyValue(donutCircle.strokeProperty(), Color.web("#2ECCFA"))),
                new KeyFrame(Duration.seconds(3), new KeyValue(donutCircle.strokeProperty(), Color.WHITE))
        );

        timeline.setAutoReverse(true); // 애니메이션 반복 설정
        timeline.setCycleCount(Timeline.INDEFINITE);
    }

    @FXML
    private void toggleAnimation() {
        if (timeline.getStatus() == Timeline.Status.RUNNING) {
            // 애니메이션 정지
            timeline.pause();
            // 정지 시 원의 테두리 색을 흰색으로 설정
            donutCircle.setStroke(Color.WHITE);
        } else {
            // 애니메이션 시작
            timeline.play();
        }
    }
}
