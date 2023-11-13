package com.example.todo_timer;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class TimerManager {

    private Timeline timeline; //JavaFX Timeline 객체, 초를 업데이트 하는데 사용
    private int seconds; //초를 저장하는 필드

    /**
     * TimerManager 클래스의 생성자
     * 타이머를 초기화하고 무한 반복을 설정
     */
    public TimerManager() {
        // 초를 업데이트하는 KeyFrame이 포함된 Timeline을 생성
        // Duration.seconds(1)은 1초마다 updateTimer() 메서드를 호출
        this.timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> updateTimer()));
        // 무한 반복을 설정
        this.timeline.setCycleCount(Timeline.INDEFINITE);
        // 초를 초기화
        this.seconds = 0;
    }

    /**
     * 타이머를 시작하는 메서드
     * 기존 타이머가 실행 중이면 먼저 정지시킨 후, 새로운 타이머를 시작
     *
     * @param timerText 타이머 텍스트를 표시하는 JavaFX Text 객체
     */
    public void startTimer(Text timerText) {
        // 기존 타이머가 실행 중이면 정지
        stopTimer();
        // 타이머를 시작합니다.
        timeline.play();
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
    }

    /**
     * 타이머를 업데이트하는 메서드
     * 타이머의 경과 시간을 초 단위로 증가시키고, 분과 초로 변환하여 시간을 텍스트로 표시
     * (예: 25:03)
     */
    private void updateTimer() {
        // 타이머의 경과 시간을 1초씩 증가
        seconds++;

        // 경과 시간을 분과 초로 변환
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;

        // 시간을 텍스트로 표시합니다.
        // (예: 25:03)
        String time = String.format("%02d:%02d", minutes, remainingSeconds);

        //임시로 시간 정보를 확인하기 위한 방법 중 하나일 뿐이며, 실제 GUI 애플리케이션에서는 이 정보를 화면에 표시하는 방식으로 변경되어야 함
        // 실제로는 GUI의 Text 엘리먼트에 설정해야함
        System.out.println(time);
    }

}
