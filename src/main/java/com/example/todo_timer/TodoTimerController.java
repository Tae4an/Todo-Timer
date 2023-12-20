package com.example.todo_timer;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;


/**
 * ToDo 타이머 애플리케이션의 컨트롤러 클래스
 */
public class TodoTimerController implements Initializable {

    @FXML
    private Text timerText;  // 타이머 시간을 표시하는 Text
    @FXML
    private Button btn_start_pause;  // 시작/일시정지 버튼. 레이블은 상태에 따라 동적으로 변경 됨
    @FXML
    private Button main_btn;  // 메인 홈 버튼
    @FXML
    private AnchorPane timer_layout;   // 타이머 홈의 레이아웃
    @FXML
    private ChoiceBox<Task> taskChoiceBox;   // 타이머 홈의 초이스 박스
    @FXML
    private Label timer_label;  // 타이머 홈 라벨
    @FXML
    private Arc timerArc; // Arc 객체 참조
    @FXML
    private Button timerSetting_btn; // 타이머 시간 설정 버튼

    private Timeline timer;  // JavaFX의 Timeline 클래스를 사용한 타이머 인스턴스. 작업 시간을 측정하고 업데이트하는 데 사용
    private int minutes = 25;  // 타이머의 초기 분 설정. 기본값은 25분
    private int seconds = 0;  // 타이머의 초기 초 설정. 기본값은 0초
    private boolean isPaused = false;  // 타이머의 일시정지 상태를 나타내는 플래그. 일시정지 상태인 경우 true로 설정
    private boolean isRest = false;  // 타이머의 휴식 상태를 나타내는 플래그
    private int newMinutes = 25; // 변경된 작업 타이머 시간
    private int newRestMinutes = 5; // 변경된 휴식 타이머 시간

    Color color = Color.rgb(255, 255, 255); // RGB 값을 사용


    /**
     * TodoTimerController의 생성자.
     */
    public TodoTimerController() {
        // TodoTaskController의 인스턴스
        TodoTaskController todoTaskController = new TodoTaskController();
    }

    /**
     * 화면 초기화 시 호출되는 메서드
     * 주요 컴포넌트들을 초기화하고 이벤트 핸들러를 등록
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // "할 일 목록" 버튼에 대한 액션 설정
        main_btn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (timer.getStatus().equals(Timeline.Status.RUNNING) && !isPaused) {
                    // 타이머가 실행 중일 때 확인 창을 표시
                    Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmAlert.setTitle("타이머 실행 중");
                    confirmAlert.setHeaderText("타이머가 아직 실행 중입니다. ");
                    confirmAlert.setContentText("타이머가 초기화 됩니다. 메인 홈으로 돌아가시겠습니까?");

                    // 다이얼로그 패널에 접근 >> 신창영
                    DialogPane dialogPane = confirmAlert.getDialogPane();
                    dialogPane.getStylesheets().add(getClass().getResource("/css/TodoTimer.css").toExternalForm());
                    dialogPane.getStyleClass().add("custom-dialog");

                    // 확인 및 취소 버튼 설정
                    Optional<ButtonType> result = confirmAlert.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        // 사용자가 '확인'을 선택한 경우
                        loadMainScene(); // 메인 홈으로 이동하는 메소드
                    }
                    // '취소'를 선택하거나 창을 닫으면 아무것도 하지 않음
                } else {
                    // 타이머가 실행 중이 아닐 때 메인 홈으로 바로 이동
                    loadMainScene(); // 메인 홈으로 이동하는 메소드
                }
            }

            private void loadMainScene() {
                // 여기에 메인 홈으로 이동하는 코드를 작성
                Parent todoMainScene;
                try {
                    todoMainScene = FXMLLoader.load(getClass().getResource("TodoMain.fxml"));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                StackPane root = (StackPane) timer_layout.getScene().getRoot();
                todoMainScene.setScaleX(3); // 초기 스케일을 크게 설정
                todoMainScene.setScaleY(3); // 초기 스케일을 크게 설정
                root.getChildren().add(todoMainScene);

                // 새 씬(메인 홈)에 줌 아웃 애니메이션 적용
                ScaleTransition zoomOut = new ScaleTransition(Duration.millis(300), todoMainScene);
                zoomOut.setToX(1); // 최종적으로 정상 크기로
                zoomOut.setToY(1); // 최종적으로 정상 크기로
                zoomOut.play(); // 애니메이션 실행
            }
        });

        // 할 일 선택 상자에 할 일 목록을 설정합니다.
            taskChoiceBox.setItems(ProjectManager.getAllTasks());
            // 선택 상자의 기본 선택을 첫 번째 항목으로 설정
            taskChoiceBox.getSelectionModel().selectFirst();

        // 타이머 초기화
        initializeTimer();

        // #FFD8D8 색상으로 아크 색상 변경
        timerArc.setFill(color); // 아크 색상 설정
        // 타이머 텍스트를 앞 쪽으로 오게 함
        timerText.toFront();

    }

    /**
     * 타이머를 초기화하는 메서드.
     * 1초 간격으로 이벤트를 실행하는 Timeline을 생성하고, 타이머를 무한히 반복하도록 설정합니다.
     * 타이머 이벤트에서는 일시정지 상태가 아니라면 타이머를 업데이트하고, 시간이 종료되면 휴식 또는 작업 타이머를 시작합니다.
     */
    private void initializeTimer() {
        setupDonutCircle();
        // 타이머 초기화: 1초 간격으로 이벤트를 실행하는 Timeline 생성
        timer = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            // 타이머가 일시정지 상태가 아닌 경우에만 처리
            if (!isPaused) {
                updateTimerDisplay();  // 타이머 디스플레이 업데이트

                // 작업 타이머가 종료된 경우
                if (minutes == 0 && seconds == 0) {
                    if (!isRest) {  // 현재 휴식 중이 아닌 경우
                        showPopup("휴식 시간입니다.", newRestMinutes +"분 동안 휴식하세요.");
                        startRestTimer();  // 휴식 타이머 시작
                    }
                    else {
                        // 휴식 타이머가 종료된 경우
                        showPopup("작업을 시작하세요.", newMinutes + "분 동안 집중하세요.");
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

        // Arc 업데이트
        double totalSeconds = 60 * 60; // 전체 작업 시간 (예: 25분)
        double elapsedSeconds = (60 - minutes) * 60 + (60 - seconds); // 경과 시간
        double startAngle = 90; // 시작 각도를 항상 90도로 유지
        timerArc.setStartAngle(startAngle);

        // 아크의 길이 업데이트 (0에서부터 증가)
        double length = (360.0 * (elapsedSeconds / totalSeconds))- 6;

        timerArc.setLength(length);
    }

    /**
     * 도넛 모양의 타이머 그래픽을 초기화하고 설정하는 메서드
     * 시작 각도를 90도로 설정하여 상단 중앙에서 시작하도록 하고,
     * 초기에는 아무런 시각적 진행도를 가지지 않도록 설정
     */
    private void setupDonutCircle() {
        timerArc.setStartAngle(90); // 시작 각도를 90도로 설정 (상단 중앙에서 시작)
        timerArc.setLength(210);      // 아무런 시각적 진행도를 가지지 않는 상태로 초기화
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
     * "시작/일시정지" 버튼 클릭 시 호출되는 메서드
     * 타이머의 상태에 따라 시작 또는 일시정지 하고 버튼의 텍스트를 업데이트
     */
    @FXML
    private void startPauseTimer() {
        if (timer.getStatus().equals(Timeline.Status.RUNNING) && !isPaused) {
            pauseTimer();
        } else {
            startTimer();
            btn_start_pause.setText("PAUSE");
        }
    }

    /**
     * 타이머를 일시정지하는 메서드
     * 타이머를 일시정지 상태로 변경하고, 사용자에게 계속 진행할지 여부를 묻는 다이얼로그를 표시
     * 사용자가 "계속"을 선택한 경우 타이머를 계속 진행하며,
     * "종료"를 선택하거나 다이얼로그를 닫은 경우 타이머를 정지
     */
    @FXML
    private void pauseTimer() {
        isPaused = true; // 타이머 일시정지 상태로 변경


        // 사용자에게 선택을 받는 다이얼로그 표시
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle("일시정지");
        alert.setHeaderText("일시정지 상태입니다.");
        alert.setContentText("계속 작업을 진행하시겠습니까?");
        alert.getDialogPane().getStyleClass().add("confirmation-dialog");


        // 다이얼로그 패널에 접근 >> 신창영
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/css/TodoTimer.css").toExternalForm());
        dialogPane.getStyleClass().add("custom-dialog");

        ButtonType resumeButton = new ButtonType("계속");
        ButtonType stopButton = new ButtonType("종료", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(resumeButton, stopButton);

        Optional<ButtonType> result = alert.showAndWait();

        // 사용자의 선택에 따라 처리
        if (result.isPresent() && result.get() == resumeButton) {
            startTimer(); // "계속" 버튼이 선택된 경우 타이머 계속 진행
        } else {
            isRest = false;
            stopTimer(); // "종료" 버튼이나 다이얼로그를 닫은 경우 타이머 정지
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
        minutes = newMinutes;
        seconds = 0;
        isPaused = false;
        btn_start_pause.setText("START");
        updateTimerDisplay();

       // 씬의 배경 색상을 #FFD8D8로 변경
        timer_layout.setBackground(new Background(new BackgroundFill(Color.rgb(255, 216, 216), null, null)));

        timerSetting_btn.setStyle("-fx-background-color:#c98888; -fx-background-radius: 10; -fx-border-color: #865353; -fx-border-radius: 10;"); // 휴식 타이머가 시작될 때의 배경 색상으로 설정
        btn_start_pause.setStyle("-fx-background-color: #c98888; -fx-background-radius: 10; -fx-border-color: #865353; -fx-border-radius: 10;"); // 휴식 타이머가 시작될 때의 배경 색상으로 설정
        main_btn.setStyle("-fx-background-color: #c98888; -fx-background-radius: 50; -fx-border-color: #865353; -fx-border-radius: 50;"); // 휴식 타이머가 시작될 때의 배경 색상으로 설정
        taskChoiceBox.setStyle("-fx-background-color:  #c98888; -fx-background-radius: 10; -fx-border-color: #865353; -fx-border-radius: 10;");
        // 색상을 #6b0404로 설정
        timerText.setFill(Color.web("#6b0404"));
        timer_label.setTextFill(Color.web("#c98888"));


    }

    /**
     * 작업 타이머를 시작하는 메서드.
     * 작업 시간 25분 설정
     */
    private void startWorkTimer() {
        minutes = newMinutes;
        seconds = 0;
        isPaused = false;
        isRest = false;

        // 씬의 배경 색상을 #FFD8D8로 변경
        timer_layout.setBackground(new Background(new BackgroundFill(Color.rgb(255, 216, 216), null, null)));

        // 휴식 타이머가 시작될 때의 배경 색상으로 설정
        timerSetting_btn.setStyle("-fx-background-color:#c98888; -fx-background-radius: 10; -fx-border-color: #865353; -fx-border-radius: 10;"); // 휴식 타이머가 시작될 때의 배경 색상으로 설정
        btn_start_pause.setStyle("-fx-background-color: #c98888; -fx-background-radius: 10; -fx-border-color: #865353; -fx-border-radius: 10;"); // 휴식 타이머가 시작될 때의 배경 색상으로 설정
        main_btn.setStyle("-fx-background-color: #c98888; -fx-background-radius: 50; -fx-border-color: #865353; -fx-border-radius: 50;"); // 휴식 타이머가 시작될 때의 배경 색상으로 설정
        taskChoiceBox.setStyle("-fx-background-color:  #c98888; -fx-background-radius: 10; -fx-border-color: #865353; -fx-border-radius: 10;");
        // 색상을 #6b0404로 설정
        timerText.setFill(Color.web("#6b0404"));
        timer_label.setTextFill(Color.web("#c98888"));
    }

    /**
     * 휴식 타이머를 시작하는 메서드.
     * 휴식 시간 5분 설정
     */
    private void startRestTimer() {
        minutes = newRestMinutes;
        seconds = 0;
        isPaused = false;
        isRest = true;

        //아이디 지정
        taskChoiceBox.setId("custom-choice-box");
        taskChoiceBox.getStylesheets().add(getClass().getResource("/css/rest.css").toExternalForm());

        // 씬의 배경 색상을 #B7F0B1로 변경
        timer_layout.setBackground(new Background(new BackgroundFill(Color.rgb(183, 240, 177), null, null)));

        timerSetting_btn.setStyle("-fx-background-color:#47C83E; -fx-background-radius: 10; -fx-border-color: #0B7903; -fx-border-radius: 10;");
        btn_start_pause.setStyle("-fx-background-color:#47C83E; -fx-background-radius: 10; -fx-border-color: #0B7903; -fx-border-radius: 10;");
        main_btn.setStyle("-fx-background-color: #47C83E; -fx-border-color: #0B7903; -fx-border-radius: 50; -fx-background-radius: 50;");
        taskChoiceBox.setStyle("-fx-background-color:  #47C83E; -fx-background-radius: 10; -fx-border-color: #0B7903; -fx-border-radius: 10;");
        timerText.setFill(Color.web("#005C00"));
        timer_label.setTextFill(Color.web("#47C83E"));
    }



    /**
     * 타이머 설정 및 휴식 시간 설정 다이얼로그를 열고 사용자 선택을 처리하는 메서드
     */
    @FXML
    private void openTimerSettingDialog() {
        if (timer.getStatus().equals(Timeline.Status.RUNNING)){
            showPopup("타이머 실행 중","타이머 실행 중엔 타이머 시간을 변경할 수 없습니다..!");
        }
        else {
            // 다이얼로그에 타이머 및 휴식 시간 설정 버튼을 포함하는 커스텀 다이얼로그를 생성합니다.
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("설정 선택");
            dialog.setHeaderText("원하는 설정을 선택하세요.");

            // 타이머 시간 설정 버튼
            ButtonType timerSettingButtonType = new ButtonType("타이머 시간 설정");
            dialog.getDialogPane().getButtonTypes().add(timerSettingButtonType);

            // 휴식 시간 설정 버튼
            ButtonType restSettingButtonType = new ButtonType("휴식 시간 설정");
            dialog.getDialogPane().getButtonTypes().add(restSettingButtonType);

            // 취소 버튼
            ButtonType cancelButtonType = new ButtonType("취소", ButtonBar.ButtonData.CANCEL_CLOSE);
            dialog.getDialogPane().getButtonTypes().add(cancelButtonType);

            // 다이얼로그 패널에 접근 >> 신창영
            DialogPane dialogPane = dialog.getDialogPane();

            dialogPane.getStylesheets().add(getClass().getResource("/css/TodoTimer.css").toExternalForm());
            dialogPane.getStyleClass().add("custom-dialog");

            // 사용자가 어떤 설정을 선택했는지 확인
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == timerSettingButtonType) {
                    openTimerInputDialog();
                } else if (dialogButton == restSettingButtonType) {
                    openRestInputDialog();
                }
                return null;
            });

            dialog.showAndWait();
        }

    }


    /**
     * 사용자로부터 새로운 작업 타이머 시간을 입력받는 다이얼로그를 열고 입력을 처리하는 메서드
     */
    private void openTimerInputDialog() {
        // 새로운 작업 타이머 시간을 입력받을 다이얼로그 생성
        TextInputDialog dialog = new TextInputDialog(Integer.toString(newMinutes)); // 현재 설정된 시간을 기본값으로 설정
        dialog.setTitle("타이머 시간 설정");
        dialog.setHeaderText("1분에서 60분 사이의 시간을 입력하세요.");
        dialog.setContentText("타이머 시간(분)을 입력하세요:");

        DialogPane dialogPane = dialog.getDialogPane();

        dialogPane.getStylesheets().add(getClass().getResource("/css/TodoTimer.css").toExternalForm());
        dialogPane.getStyleClass().add("custom-dialog");

        // 다이얼로그를 표시하고 사용자 입력을 처리
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(time -> {
            try {
                newMinutes = Integer.parseInt(time);
                if (newMinutes >= 1 && newMinutes <= 60) {
                    // 입력된 값이 1 이상인 경우에만 시간을 설정
                    minutes = newMinutes;
                    seconds = 0;
                    updateTimerDisplay();
                } else {
                    showPopup("오류", "1분에서 60분 사이의 유효한 시간을 입력하세요.");
                }
            } catch (NumberFormatException e) {
                showPopup("오류", "유효한 숫자를 입력하세요.");
            }
        });
    }



    /**
     * 사용자로부터 새로운 휴식 시간을 입력받는 다이얼로그를 열고 입력을 처리하는 메서드
     */
    private void openRestInputDialog() {
        // 새로운 휴식 시간을 입력받을 다이얼로그 생성
        TextInputDialog dialog = new TextInputDialog(Integer.toString(newRestMinutes)); // 기본값은 5분
        dialog.setTitle("휴식 시간 설정");
        dialog.setHeaderText("1분에서 30분 사이의 시간을 입력하세요.");
        dialog.setContentText("휴식 시간(분)을 입력하세요:");

        DialogPane dialogPane = dialog.getDialogPane();

        dialogPane.getStylesheets().add(getClass().getResource("/css/TodoTimer.css").toExternalForm());
        dialogPane.getStyleClass().add("custom-dialog");

        // 다이얼로그를 표시하고 사용자 입력을 처리
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(time -> {
            try {
                newRestMinutes = Integer.parseInt(time);
                if (newRestMinutes >= 1 && newRestMinutes <= 30) {
                    // 입력된 값이 1 이상인 경우에만 휴식 시간을 설정
                    seconds = 0;
                } else {
                    showPopup("오류", "1분에서 25분 사이의 유효한 시간을 입력하세요.");
                }
            } catch (NumberFormatException e) {
                showPopup("오류", "유효한 숫자를 입력하세요.");
            }
        });
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


}