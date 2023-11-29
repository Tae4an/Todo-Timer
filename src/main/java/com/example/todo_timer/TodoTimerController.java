package com.example.todo_timer;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.Optional;

/**
 * ToDo 타이머 애플리케이션의 컨트롤러 클래스
 * 마지막 수정 일자 : 2023.11.29
 */
public class TodoTimerController {

    @FXML
    private ListView<String> taskListView;  // 작업 목록을 나타내는 ListView. 사용자가 작업을 추가, 수정, 삭제할 때 업데이트 됨

    @FXML
    private TextField taskInput;  // 사용자가 새 작업을 입력하는 TextField. "추가" 버튼을 클릭하면 작업 목록에 새로운 작업이 추가

    @FXML
    private Text timerText;  // 타이머 시간을 표시하는 Text. 타이머가 감소할 때마다 업데이트되며, 작업 시간을 나타냄

    @FXML
    private Button btn_setting;  // 설정 버튼. 현재는 버튼의 동작이 구현되어 있지 않음

    @FXML
    private Button btn_start_pause;  // 시작/일시정지 버튼. 타이머를 시작하거나 일시정지할 때 사용됩니다. 버튼의 레이블은 상태에 따라 동적으로 변경 됨


    private final TaskManager taskManager;  // 작업 관리자(TaskManager) 인스턴스. 작업 목록을 관리하는 데 사용됨

    private Timeline timer;  // JavaFX의 Timeline 클래스를 사용한 타이머 인스턴스. 작업 시간을 측정하고 업데이트하는 데 사용됨

    private int minutes = 25;  // 타이머의 초기 분 설정. 기본값은 25분

    private int seconds = 0;  // 타이머의 초기 초 설정. 기본값은 0초

    private boolean isPaused = false;  // 타이머의 일시정지 상태를 나타내는 플래그. 일시정지 상태인 경우 true로 설정


    /**
     * TodoTimerController의 생성자.
     * 설정 버튼을 매개변수로 받아 초기화하며, TaskManager와 Timer를 설정합
     *
     * @param btnSetting 설정 버튼. TodoTimerController에 설정 버튼을 전달하고 초기화
     */
    public TodoTimerController(Button btnSetting) {
        // 전달 받은 설정 버튼을 클래스 필드에 할당
        // TaskManager 초기화
        this.taskManager = new TaskManager();
        // 타이머 초기화
        initializeTimer();
    }

    /**
     * 타이머를 초기화하는 메서드.
     * 1초 간격으로 이벤트를 실행하는 Timeline을 생성하고, 타이머를 무한히 반복하도록 설정합니다.
     * 타이머 이벤트에서는 일시정지 상태가 아니라면 타이머를 업데이트하고, 시간이 종료되면 타이머를 중지
     */
    private void initializeTimer() {
        // 타이머 초기화: 1초 간격으로 이벤트를 실행하는 Timeline 생성
        timer = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            if (!isPaused) {
                updateTimerDisplay();
                if (minutes == 0 && seconds == 0) {
                    stopTimer();
                } else {
                    decrementTime();
                }
            }
        }));
        timer.setCycleCount(Timeline.INDEFINITE); // 타이머를 무한히 반복
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
        minutes = 25;
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
            btn_start_pause.setText("시작");
        } else {
            startTimer();
            btn_start_pause.setText("일시정지");
        }
    }
}