module com.example.todo_timer {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.todo_timer to javafx.fxml;
    exports com.example.todo_timer;
}