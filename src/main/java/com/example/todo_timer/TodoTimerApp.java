package com.example.todo_timer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * ToDo 타이머 애플리케이션의 진입점인 메인 애플리케이션 클래스
 */
public class TodoTimerApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // TodoTimer.fxml 파일에서 레이아웃을 불러와서 사용
        Parent root = FXMLLoader.load(getClass().getResource("TodoTimer.fxml"));
        primaryStage.setTitle("ToDo 타이머");
        primaryStage.setScene(new Scene(root, 300, 200));
        primaryStage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}
