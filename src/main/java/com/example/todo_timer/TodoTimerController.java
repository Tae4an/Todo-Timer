package com.example.todo_timer;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
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
    private Text timerText;  // 타이머 시간을 표시하는 Text
    @FXML
    private Button btn_start_pause;  // 시작/일시정지 버튼. 레이블은 상태에 따라 동적으로 변경 됨
    @FXML
    private Button tsk_btn;  // 작업 홈 버튼
    @FXML
    private AnchorPane timer_layout;   // 타이머 홈의 레이아웃
    @FXML
    private ChoiceBox<String> taskChoiceBox;   // 타이머 홈의 초이스 박스


    private Timeline timer;  // JavaFX의 Timeline 클래스를 사용한 타이머 인스턴스. 작업 시간을 측정하고 업데이트하는 데 사용

    private int minutes = 25;  // 타이머의 초기 분 설정. 기본값은 25분

    private int seconds = 0;  // 타이머의 초기 초 설정. 기본값은 0초

    private boolean isPaused = false;  // 타이머의 일시정지 상태를 나타내는 플래그. 일시정지 상태인 경우 true로 설정

    private boolean isRest = false;  // 타이머의 휴식 상태를 나타내는 플래그

    private final TodoTaskController todoTaskController;




    /**
     * 화면 초기화 시 호출되는 메서드
     * 주요 컴포넌트들을 초기화하고 이벤트 핸들러를 등록
     *
     * @param location   FXML 파일의 위치
     * @param resources  리소스 번들
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // "할 일 목록" 버튼에 대한 액션 설정
        tsk_btn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                StackPane stackPane = (StackPane) timer_layout.getScene().getRoot();
                Parent sub = (Parent) stackPane.getChildren().get(1);

                Timeline timeline = new Timeline();
                KeyValue keyValue = new KeyValue(sub.translateYProperty(), 400);
                KeyFrame keyFrame = new KeyFrame(Duration.millis(300), new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        stackPane.getChildren().remove(1);
                    }
                }, keyValue);
                timeline.getKeyFrames().add(keyFrame);
                timeline.play();


            }
        });

        // 할 일 선택 상자에 할 일 목록을 설정합니다.
        taskChoiceBox.setItems(todoTaskController.getTasks());
        // 선택 상자의 기본 선택을 첫 번째 항목으로 설정
        taskChoiceBox.getSelectionModel().selectFirst();

        // 타이머 초기화
        initializeTimer();
    }
    /**
     * TodoTimerController의 생성자.
     */
    public TodoTimerController() {
        // TodoTaskController 인스턴스를 얻어옴
        this.todoTaskController = TodoTaskController.getInstance();
        initializeTimer(); // 타이머 초기화
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

//    /**
//     * "일시정지" 버튼 클릭 시 호출되는 메서드
//     * 타이머를 일시정지 상태로 변경
//     */
//    @FXML
//    private void pauseTimer() {
//        // 타이머 일시정지 메서드
//        isPaused = true;
//    }


    /**
     * "시작/일시정지" 버튼 클릭 시 호출되는 메서드
     * 타이머의 상태에 따라 시작 또는 일시정지 하고 버튼의 텍스트를 업데이트
     */
    @FXML
    private void startPauseTimer() {
        if (timer.getStatus().equals(Timeline.Status.RUNNING) && !isPaused) {
            pauseTimer();
        } else {
            startTimer();
            btn_start_pause.setText("일시정지");
        }
    }

    @FXML
    private void pauseTimer() {
        // 타이머 일시정지 메서드
        isPaused = true;

        // 사용자에게 선택을 받는 다이얼로그 표시
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("일시정지");
        alert.setHeaderText("일시정지 상태입니다.");
        alert.setContentText("계속 작업을 진행하시겠습니까?");

        ButtonType resumeButton = new ButtonType("계속");
        ButtonType stopButton = new ButtonType("정지", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(resumeButton, stopButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == resumeButton) {
            // "계속" 버튼이 선택된 경우
            startTimer();  // 타이머 계속 진행
        } else {
            // "정지" 버튼이나 다이얼로그를 닫은 경우
            stopTimer();  // 타이머 정지
        }
    }

    /**
     * "종료" 버튼 클릭 시 호출되는 메서드
     * 타이머를 종료하고 초기 상태로 설정
     */
    @FXML
    private void stopTimer() {
        // 타이머 종료 메서드
        timer.stop();
        minutes = 25;
        seconds = 0;
        isPaused = false;
        btn_start_pause.setText("작업 시작");
        updateTimerDisplay();
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
