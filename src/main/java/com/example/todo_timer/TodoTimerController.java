package com.example.todo_timer;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * ToDo 타이머 애플리케이션의 컨트롤러 클래스
 * 마지막 수정 일자 : 2023.11.29
 */
public class TodoTimerController implements Initializable {

    @FXML
    private Text timerText;  // 타이머 시간을 표시하는 Text. 타이머가 감소할 때마다 업데이트되며, 작업 시간을 나타냄

    @FXML
    private Button btn_setting;  // 설정 버튼. 현재는 버튼의 동작이 구현되어 있지 않음

    @FXML
    private Button btn_start_pause;  // 시작/일시정지 버튼. 타이머를 시작하거나 일시정지할 때 사용됩니다. 버튼의 레이블은 상태에 따라 동적으로 변경 됨



    private Timeline timer;  // JavaFX의 Timeline 클래스를 사용한 타이머 인스턴스. 작업 시간을 측정하고 업데이트하는 데 사용됨

    private int minutes = 25;  // 타이머의 초기 분 설정. 기본값은 25분

    private int seconds = 0;  // 타이머의 초기 초 설정. 기본값은 0초

    private boolean isPaused = false;  // 타이머의 일시정지 상태를 나타내는 플래그. 일시정지 상태인 경우 true로 설정

    private boolean isRest = false;  // 타이머의 휴식 상태를 나타내는 플래그


    @FXML
    private Button tsk_btn;
    @FXML
    private AnchorPane timer_layout;



    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tsk_btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                AnchorPane root = (AnchorPane) tsk_btn.getScene().getRoot();
                root.getChildren().clear();  // 현재 화면의 모든 자식 노드를 제거
                // TodoTask.fxml을 불러와서 root에 추가
                try {
                    Parent todoTask = FXMLLoader.load(getClass().getResource("TodoTask.fxml"));
                    root.getChildren().add(todoTask);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    /**
     * TodoTimerController의 생성자.
     */
    public TodoTimerController() {
        // 타이머 초기화
        initializeTimer();
    }

    /**
     * 타이머를 초기화하는 메서드.
     * 1초 간격으로 이벤트를 실행하는 Timeline을 생성하고, 타이머를 무한히 반복하도록 설정합니다.
     * 타이머 이벤트에서는 일시정지 상태가 아니라면 타이머를 업데이트하고, 시간이 종료되면 휴식 또는 작업 타이머를 시작합니다.
     */
    private void initializeTimer() {
        // 타이머 초기화: 1초 간격으로 이벤트를 실행하는 Timeline 생성
        timer = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            // 타이머가 일시정지 상태가 아닌 경우에만 처리
            if (!isPaused) {
                updateTimerDisplay();  // 타이머 디스플레이 업데이트

                // 작업 타이머가 종료된 경우
                if (minutes == 0 && seconds == 0) {
                    if (!isRest) {  // 현재 휴식 중이 아닌 경우
                        showPopup("휴식 시간입니다.", "5분 동안 휴식하세요.");
                        startRestTimer();  // 휴식 타이머 시작
                    } else {
                        // 휴식 타이머가 종료된 경우
                        showPopup("작업을 시작하세요.", "25분 동안 집중하세요.");
                        startWorkTimer();  // 작업 타이머 시작
                    }
                } else {
                    decrementTime();  // 타이머 시간 감소
                }
            }
        }));

        timer.setCycleCount(Timeline.INDEFINITE); // 타이머를 무한히 반복하도록 설정
    }

    /**
     * 타이머 텍스트를 업데이트하는 메서드
     */
    private void updateTimerDisplay() {
        // 타이머 텍스트 업데이트
        timerText.setText(String.format("%02d:%02d", minutes, seconds));
    }

    /**
     * 시간을 감소시키는 메서드
     */
    private void decrementTime() {
        if (seconds == 0) {
            minutes--;
            seconds = 59;
        } else {
            seconds--;
        }
    }


    /**
     * "시작" 버튼 클릭 시 호출되는 메서드
     * 타이머를 시작하고, 일시정지 상태를 해제
     */
    @FXML
    private void startTimer() {
        // 타이머 시작 메서드
        timer.play();
        isPaused = false;
    }

    /**
     * "일시정지" 버튼 클릭 시 호출되는 메서드
     * 타이머를 일시정지 상태로 변경
     */
    @FXML
    private void pauseTimer() {
        // 타이머 일시정지 메서드
        isPaused = true;
    }

    /**
     * "종료" 버튼 클릭 시 호출되는 메서드
     * 타이머를 종료하고 초기 상태로 설정
     */
    @FXML
    private void stopTimer() {
        // 타이머 종료 메서드
        timer.stop();
        minutes = 1;
        seconds = 0;
        isPaused = false;
        updateTimerDisplay();
    }

    /**
     * "시작/일시정지" 버튼 클릭 시 호출되는 메서드
     * 타이머의 상태에 따라 시작 또는 일시정지 하고 버튼의 텍스트를 업데이트
     */
    @FXML
    private void startPauseTimer() {
        if (timer.getStatus().equals(Timeline.Status.RUNNING) && !isPaused) {
            pauseTimer();
            btn_start_pause.setText("작업 시작");
        } else {
            startTimer();
            btn_start_pause.setText("일시정지");
        }
    }



    /**
     * 작업 타이머를 시작하는 메서드.
     * 작업 시간 25분 설정
     */
    private void startWorkTimer() {
        minutes = 25;
        seconds = 0;
        isPaused = false;
    }

    /**
     * 휴식 타이머를 시작하는 메서드.
     * 휴식 시간 5분 설정
     */
    private void startRestTimer() {
        minutes = 5;
        seconds = 0;
        isPaused = false;
        isRest = true;
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