package com.example.todo_timer;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.text.Text;
import javafx.util.Duration;  // 이 부분을 수정

import java.util.Optional;

/**
 * ToDo 타이머 애플리케이션의 컨트롤러 클래스
 */
public class TodoTimerController {

    @FXML
    private ListView<String> taskListView;

    @FXML
    private TextField taskInput;

    @FXML
    private Text timerText;

    // 작업 관리자(TaskManager) 및 타이머 관리자(TimerManager) 인스턴스
    private final TaskManager taskManager;
    private final TimerManager timerManager;

    // JavaFX에서 제공하는 타이머 객체
    private Timeline timer;

    // 경과 시간을 나타내는 Duration 객체
    private Duration timeElapsed = Duration.ZERO;

    // 타이머 실행 여부를 나타내는 플래그
    private boolean isTimerRunning;

    /**
     * TodoTimerController의 생성자
     * TaskManager와 TimerManager 인스턴스를 초기화하고,
     * 타이머를 설정
     */
    public TodoTimerController() {
        // TaskManager 초기화
        this.taskManager = new TaskManager();

        // TimerManager 초기화
        this.timerManager = new TimerManager();

        // 타이머 설정
        // 초당 한 번씩 이벤트를 발생시키는 KeyFrame을 가지는 Timeline을 생성
        // 해당 이벤트는 updateTimerText() 메서드를 호출하여 타이머 텍스트를 업데이트
        this.timer = new Timeline(new KeyFrame(Duration.seconds(1), event -> updateTimerText()));

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
        // 타이머가 실행 중이 아닌 경우에만 시작
        if (!isTimerRunning) {
            // 타이머를 무한 반복으로 설정하고 실행
            timer.setCycleCount(Timeline.INDEFINITE);
            timer.play();
            // 타이머 실행 상태를 갱신
            isTimerRunning = true;
        }
    }

    /**
     * "정지" 버튼 클릭 시 호출되는 메서드
     * 타이머가 실행 중인 경우에만 타이머를 일시 정지시키고, 상태를 갱신
     */
    @FXML
    private void stopTimer() {
        // 타이머가 현재 실행 중인지 확인합니다.
        if (isTimerRunning) {
            // 타이머를 일시 정지
            timer.pause();
            // 타이머가 정지 되었음을 상태 변수에 반영
            isTimerRunning = false;
        }
    }
}
