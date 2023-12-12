module com.example.todo_timer {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;


    opens com.example.todo_timer to javafx.fxml;
    exports com.example.todo_timer;
}