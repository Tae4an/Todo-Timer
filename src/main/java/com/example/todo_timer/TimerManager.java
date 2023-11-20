package com.example.todo_timer;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * TimerManager 클래스는 타이머를 관리하는 클래스
 */
public class TimerManager {

    private Timeline timeline; //JavaFX Timeline 객체, 초를 업데이트 하는데 사용
    private int seconds; //초를 저장하는 필드
    private Text timerText; // 타이머 텍스트를 표시하는 JavaFX Text 객체
    private boolean isWorking = false; // 현재 작업 중인지 여부를 나타내는 플래그
    private boolean timerRunning; // 타이머가 현재 실행 중인지 여부를 나타내는 플래그

    /**
     * TimerManager 클래스의 생성자
     *
     * @param timerText 타이머 텍스트를 표시하는 JavaFX Text 객체
     */
    public TimerManager(Text timerText) {
        this.timerText = timerText; // timerText 필드 초기화
        updateTimer();
        // 초를 업데이트하는 KeyFrame이 포함된 Timeline을 생성
        // Duration.seconds(1)은 1초마다 updateTimer() 메서드를 호출
        this.timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> updateTimer()));
        // 무한 반복을 설정
        this.timeline.setCycleCount(Timeline.INDEFINITE);
        // 초를 초기화
        this.seconds = 0;

        initializeTimer();

        this.timerRunning = false;
    }

    /**
     * 타이머를 시작하는 메서드
     * 기존 타이머가 실행 중이면 먼저 정지시킨 후, 새로운 타이머를 시작
     */
    public void startTimer() {
        // 기존 타이머가 실행 중이면 정지
        stopTimer();

        // 타이머가 실행 중이 아니면 시작하고, 실행 중이면 일시 정지
        if (!timerRunning) {
            timerRunning = true;
            timeline.play();
        } else {
            timerRunning = false;
            timeline.pause();
        }
    }

    /**
     * 타이머를 정지하는 메서드
     * 타이머를 정지하고, 타이머의 경과 시간을 초기화
     */
    public void stopTimer() {
        // 타이머를 정지
        timeline.stop();
        // 타이머의 경과 시간을 초기화
        seconds = 0;
        // 텍스트 업데이트
        updateTimerText();
    }


    /**
     * 타이머를 업데이트하는 메서드
     * 타이머의 경과 시간을 초 단위로 증가시키고, 분과 초로 변환하여 시간을 텍스트로 표시
     */
    private void updateTimer() {
        // 타이머의 경과 시간을 1초씩 증가
        seconds++;

        // 현재 작업 중인지 여부를 확인하여 작업 중이면 25분, 휴식 중이면 5분으로 설정
        int targetTime = isWorking ? 25 * 60 : 5 * 60;

        // 경과 시간이 목표 시간을 초과하지 않도록 조정
        seconds = Math.min(seconds, targetTime);

        // 경과 시간을 분과 초로 변환
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;

        // 형식화된 시간 문자열을 타이머 화면에 표시
        updateTimerText();

        // 추가된 부분: 작업이 끝났을 때 휴식 타이머 시작
        if (seconds == targetTime) {
            if (isWorking) {
                showAlert("작업이 끝났습니다. 5분 휴식하세요!");
                startBreakTimer();
            } else {
                showAlert("휴식이 끝났습니다. 다시 작업을 시작하세요!");
            }
        }
    }



    /**
     * 타이머 화면의 텍스트를 업데이트하는 메서드
     * 현재 경과 시간을 분과 초로 변환하여 포맷팅하고, 타이머 화면의 텍스트 필드에 설정
     */
    private void updateTimerText() {
        if (timerText != null) {
            int minutes = seconds / 60;
            int remainingSeconds = seconds % 60;
            // 현재 경과 시간을 분과 초로 변환하여 포맷팅하는 부분
            String formattedTime = String.format("%02d:%02d", minutes, remainingSeconds);
            // 타이머 화면에 형식화된 시간 문자열을 설정하는 부분
            timerText.setText(formattedTime);
        }
    }


    /**
     * 타이머 초기화 메서드
     * 1초 간격으로 호출되는 KeyFrame을 생성하여 타이머 텍스트 업데이트 및 작업 종료 여부 확인
     */
    private void initializeTimer() {
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(1), event -> {
            // 1초마다 타이머 텍스트 업데이트
            updateTimerText();
            // 타이머 종료 여부 확인
            checkTimerFinish();
        });
        // KeyFrame을 타임라인에 추가
        timeline.getKeyFrames().add(keyFrame);
    }

    /**
     * 타이머 종료 여부를 확인하고 적절한 동작을 수행하는 메서드.
     * 현재 타이머의 경과 시간과 총 실행 시간을 비교하여 종료 여부를 판단하고,
     * 작업 중일 때는 휴식을, 휴식 중일 때는 작업을 시작하도록 합니다.
     */
    private void checkTimerFinish() {
        // 현재 타이머의 경과 시간과 총 실행 시간을 비교하여 남은 시간을 계산
        Duration remainingTime = timeline.getCurrentTime().subtract(timeline.getTotalDuration());

        // 남은 시간이 알 수 없는 값인지 확인
        if (remainingTime.isUnknown()) {
            // 작업 중인 경우
            if (isWorking) {
                // 5분 휴식 알림 표시
                showAlert("5분 휴식!");
                // 휴식 타이머 시작
                startBreakTimer();
            } else { // 휴식 중인 경우
                // 휴식 끝나고 다시 작업 시작 알림 표시
                showAlert("휴식 끄읕. 다시 작업을 시작하세요!");
                // 타이머 정지
                stopTimer();
            }
        }
    }



    /**
     * 휴식 타이머를 시작하는 메서드.
     * 작업이 끝났을 때 호출되며, 작업 상태를 휴식으로 변경하고 타이머를 정지한 후 다시 시작
     */
    private void startBreakTimer() {
        // 작업 상태를 휴식으로 변경
        isWorking = false;
        // 타이머 정지
        timeline.stop();
        // 타이머 재시작 (휴식 타이머 시작)
        timeline.play();
    }

    /**
     * 사용자에게 알림을 표시하는 메서드.
     *
     * @param message 표시할 알림 메시지
     */
    private void showAlert(String message) {
        // 여기에 알림을 표시하는 로직을 추가
        System.out.println(message);
    }


    /**
     * 타이머가 현재 실행 중인지 여부를 반환하는 메서드.
     *
     * @return 타이머가 실행 중이면 true, 그렇지 않으면 false
     */
    public boolean isTimerRunning() {
        return timerRunning;
    }

}
