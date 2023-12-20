package com.example.todo_timer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

/**
 * ToDo 타이머 애플리케이션의 진입점인 메인 애플리케이션 클래스
 */
public class TodoTimerApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        //Objects.requireNonNull은 getResource("TodoTimer.fxml")의 반환값이 null이면 예외를 발생시키므로,
        // 리소스가 없는 경우에는 예외가 발생
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("TodoMain.fxml")));
        primaryStage.setTitle("ToDo 타이머");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}