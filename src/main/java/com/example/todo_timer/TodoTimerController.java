package com.example.todo_timer;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;  // 이 부분을 수정

import java.util.Optional;

/**
 * ToDo 타이머 애플리케이션의 컨트롤러 클래스
 */
public class TodoTimerController {
    @FXML
    private Button startButton;     // 작업 시작 버튼
    @FXML
    private Button stopButton;  // 작업 정지 버튼
    @FXML
    private ListView<String> taskListView;  // 작업 목록을 표시하는 리스트 뷰
    @FXML
    private TextField taskInput;     // 새 작업을 입력하는 텍스트 필드
    @FXML
    private Text timerText;  // 타이머의 시간을 표시하는 텍스트
    private final TaskManager taskManager;    // 작업 관리자(TaskManager) 인스턴스
    private TimerManager timerManager;    // 타이머 관리자(TimerManager) 인스턴스
    private Timeline timer;      // JavaFX에서 제공하는 타이머 객체
    private Duration timeElapsed = Duration.ZERO;   // 경과 시간을 나타내는 Duration 객체
    private boolean isTimerRunning;     // 타이머 실행 여부를 나타내는 플래그
    private boolean isWorking = false;      // 현재 작업 중인지 여부를 나타내는 플래그


    /**
     * TodoTimerController의 생성자
     * TaskManager와 TimerManager 인스턴스를 초기화하고,
     * 타이머를 설정
     */
    public TodoTimerController() {
        // TaskManager 초기화
        this.taskManager = new TaskManager();

        // 타이머 설정
        // 초당 한 번씩 이벤트를 발생시키는 KeyFrame을 가지는 Timeline을 생성
        // 해당 이벤트는 updateTimerText() 메서드를 호출하여 타이머 텍스트를 업데이트
        this.timer = new Timeline(new KeyFrame(Duration.seconds(1), event -> Platform.runLater(() -> updateTimerText())));

        // TimerManager 초기화
        this.timerManager = new TimerManager(timerText);

        // 타이머가 현재 실행 중인지 나타내는 플래그를 초기화
        this.isTimerRunning = false;

        // 경과 시간을 나타내는 Duration을 초기화
        this.timeElapsed = Duration.ZERO;
    }


    /**
     * "추가" 버튼 클릭 시 호출되는 메서드
     * 입력된 작업을 작업 목록에 추가하고, 입력 필드를 비움
     */
    @FXML
    private void addTask() {
        // TaskManager를 사용하여 작업 목록에 새로운 작업을 추가
        taskManager.addTask(taskListView, taskInput.getText());
        // 작업 추가 후, 입력 필드를 비워 사용자에게 새 작업을 입력할 수 있도록 함
        taskInput.clear();
    }

    /**
     * "수정" 버튼 클릭 시 호출되는 메서드
     * 선택된 작업을 수정하도록 다이얼로그를 표시하고,
     * 수정된 작업을 TaskManager를 통해 업데이트
     */
    @FXML
    private void editTask() {
        // 선택된 작업의 인덱스를 가져옴
        int selectedIndex = taskListView.getSelectionModel().getSelectedIndex();

        // 선택된 작업이 있는 경우에만 수정 다이얼로그를 표시
        if (selectedIndex >= 0) {
            // 선택된 작업의 현재 내용을 가져옴
            String currentTask = taskListView.getItems().get(selectedIndex);

            // 수정 다이얼로그를 표시하고, 사용자가 입력한 내용을 받음
            Optional<String> result = showEditDialog(currentTask);

            // 사용자가 내용을 입력한 경우에만 TaskManager를 통해 작업을 수정
            result.ifPresent(updatedTask -> taskManager.editTask(taskListView, selectedIndex, updatedTask));
        }
    }

    /**
     * "삭제" 버튼 클릭 시 호출되는 메서드
     * 선택된 작업을 TaskManager를 통해 삭제
     */
    @FXML
    private void deleteTask() {
        // 선택된 작업의 인덱스를 가져옴
        int selectedIndex = taskListView.getSelectionModel().getSelectedIndex();

        // 선택된 작업이 있는 경우에만 TaskManager를 통해 해당 작업을 삭제
        if (selectedIndex >= 0) {
            taskManager.deleteTask(taskListView, selectedIndex);
        }
    }

    /**
     * 작업을 수정하기 위한 다이얼로그를 표시하는 메서드
     *
     * @param currentTask 수정할 작업의 현재 내용
     * @return 사용자가 입력한 새로운 작업 내용(Optional).
     */
    private Optional<String> showEditDialog(String currentTask) {
        TextInputDialog dialog = new TextInputDialog(currentTask);
        dialog.setTitle("작업 수정");
        dialog.setHeaderText("수정할 작업");
        dialog.setContentText("새로운 작업:");

        return dialog.showAndWait();
    }

    /**
     * 타이머를 업데이트하는 메서드
     * 1초마다 호출되며, 경과 시간을 1초씩 더하고 형식화하여 타이머 화면에 표시
     */
    private void updateTimerText() {
        // 확인을 위한 출력문 추가
        System.out.println("Updating timer text");

        // 경과 시간에 1초를 더함
        timeElapsed = timeElapsed.add(Duration.seconds(1));
        // 형식화된 시간 문자열을 타이머 화면에 표시
        timerText.setText(formatTime(timeElapsed));
    }

    /**
     * 주어진 Duration을 형식화하는 메서드
     * Duration을 분과 초로 변환하여 문자열로 반환
     *
     * @param duration 형식화할 Duration 객체
     * @return 형식화된 시간 문자열 (mm:ss)
     */
    private String formatTime(Duration duration) {
        // 확인을 위한 출력문
        System.out.println("Formatting time");

        // Duration을 초로 변환
        long seconds = (long) duration.toSeconds();
        // 초를 절댓값으로 변환
        long absSeconds = Math.abs(seconds);
        // 분을 계산
        long minutes = absSeconds / 60;
        // 남은 초를 계산
        long remainingSeconds = absSeconds % 60;
        // 시간을 음수로 표현할 경우에는 음수 부호를 붙임
        return String.format("%02d:%02d", minutes * (seconds < 0 ? -1 : 1), remainingSeconds);
    }

    /**
     * 시작 버튼이 클릭 되었을 때 호출되는 메서드
     * 타이머가 실행 중이지 않은 경우에만 타이머를 시작
     */
    @FXML
    private void startTimer() {
        timerManager.startTimer();
    }

    /**
     * "정지" 버튼 클릭 시 호출되는 메서드
     * 타이머가 실행 중인 경우에만 타이머를 일시 정지시키고, 상태를 갱신
     */
    @FXML
    private void stopTimer() {
        timerManager.stopTimer();
        startButton.setText("작업 시작");

    }

    /**
     * 시작 버튼(`startButton`)이 클릭되었을 때 호출되는 이벤트 핸들러.
     * 타이머가 실행 중이지 않으면 작업을 시작하고, 실행 중이면 작업을 종료
     * 작업 시작 시 버튼 텍스트를 "일시정지"로 변경하고, 작업 종료 시 버튼 텍스트를 "작업 시작"으로 변경
     * 작업이 시작될 때는 `isWorking` 변수를 `true`로 설정하고, 작업이 종료될 때는 `isWorking` 변수를 `false`로 설정
     *
     * @param event 시작 버튼 클릭 이벤트
     */
    @FXML
    private void handleStartButton(ActionEvent event) {
        // startButton의 텍스트를 업데이트하고, isWorking 변수를 토글
        if (!timerManager.isTimerRunning()) {
            // 타이머가 실행 중이 아니라면 작업 시작
            timerManager.startTimer();
            // 버튼 텍스트 업데이트
            startButton.setText("일시정지");
            // 작업 중 상태로 설정
            isWorking = true;
        } else {
            // 타이머가 실행 중이라면 작업 종료
            timerManager.stopTimer();
            // 버튼 텍스트 업데이트
            startButton.setText("작업 시작");
            // 작업 중이 아닌 상태로 설정
            isWorking = false;
        }
    }

    /**
     * 종료 버튼(`stopButton`)이 클릭되었을 때 호출되는 이벤트 핸들러.
     * 타이머를 정지하고, 작업 시작 버튼의 텍스트를 "작업 시작"으로 변경
     *
     * @param event 종료 버튼 클릭 이벤트
     */
    @FXML
    private void handleStopButton(ActionEvent event) {
        // 타이머 정지
        timerManager.stopTimer();
        // 작업 시작 버튼 텍스트 업데이트
        startButton.setText("작업 시작");
    }


    /**
     * 초기화 메서드.
     * 애플리케이션이 시작될 때 자동으로 호출되며, UI 요소를 초기화하고 이벤트 핸들러를 등록.
     * 이 메서드에서는 `timerText`를 "25:00"으로 초기화하고, 종료 버튼(`stopButton`)에 이벤트 핸들러를 등록.
     */
    @FXML
    private void initialize() {
        // 나중에 사용자가 직접 설정한 시간으로 초기화하도록 변경할 예정
        // timerText 초기화
        timerText.setText("25:00");

        // 종료 버튼에 이벤트 핸들러 등록
        stopButton.setOnAction(this::handleStopButton);
    }

}
