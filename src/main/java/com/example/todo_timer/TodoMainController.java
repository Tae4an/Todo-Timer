package com.example.todo_timer;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class TodoMainController implements Initializable {
    @FXML
    private Button tm_btn; // "타이머" 기능을 위한 버튼
    @FXML
    private Button addProject_btn; // "프로젝트 추가" 버튼
    @FXML
    private ListView<ProjectManager> projectListView; // 프로젝트 목록을 표시하는 ListView
    @FXML
    private Button manageTask_btn;

    private static ObservableList<ProjectManager> projects = FXCollections.observableArrayList();
    private final TodoTaskController todoTaskController =  TodoTaskController.getInstance();


    public TodoMainController() {
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        manageTask_btn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                // ListView에서 선택된 작업을 얻음
                ProjectManager selectedProject = projectListView.getSelectionModel().getSelectedItem();

                // 작업이 없거나 선택되지 않았을 경우 에러 메시지 표시
                if (projectListView.getItems().isEmpty()) {
                    showPopup("Error","프로젝트가 없습니다..!");

                } else if (selectedProject == null) {
                    showPopup("Error","프로젝트를 선택하세요..!");
                } else {
                    try {
                        Parent sub = FXMLLoader.load(getClass().getResource("TodoTask.fxml"));
                        StackPane root = (StackPane) manageTask_btn.getScene().getRoot();
                        root.getChildren().add(sub);
                        todoTaskController.setCurrentProject(selectedProject);



                        // 뷰에 애니메이션 효과 적용
                        sub.setTranslateX(500);
                        Timeline timeline = new Timeline();
                        KeyValue keyValue = new KeyValue(sub.translateXProperty(), 0);
                        KeyFrame keyFrame = new KeyFrame(Duration.millis(300), keyValue);
                        timeline.getKeyFrames().add(keyFrame);
                        timeline.play();

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        // "타이머" 버튼에 대한 클릭 이벤트 핸들러 설정
        tm_btn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                // TodoTimer.fxml 뷰를 로드하고 화면에 추가
                try {
                    Parent sub = FXMLLoader.load(getClass().getResource("TodoTimer.fxml"));
                    StackPane root = (StackPane) tm_btn.getScene().getRoot();
                    root.getChildren().add(sub);

                    // 뷰에 애니메이션 효과 적용
                    sub.setTranslateY(600);
                    Timeline timeline = new Timeline();
                    KeyValue keyValue = new KeyValue(sub.translateYProperty(), 0);
                    KeyFrame keyFrame = new KeyFrame(Duration.millis(300), keyValue);
                    timeline.getKeyFrames().add(keyFrame);
                    timeline.play();

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        projectListView.setItems(projects);

        addProject_btn.setOnMouseClicked(event -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("프로젝트 추가");
            dialog.setHeaderText("새 프로젝트의 이름을 입력하세요:");

            // 다이얼로그 패널에 접근 >> 신창영
            DialogPane dialogPane = dialog.getDialogPane();

            dialogPane.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            dialogPane.getStyleClass().add("custom-dialog");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(name -> {
                // 프로젝트 이름 중복 검사
                if (isProjectNameExist(name)) {
                    showPopup("중복된 프로젝트", "이미 존재하는 프로젝트 이름입니다.");
                } else {
                    ProjectManager newProject = new ProjectManager(name);
                    projects.add(newProject);
                }
            });
        });

    updateProjectList();
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
            alert.showAndWait();

            DialogPane dialogPane = alert.getDialogPane();

            dialogPane.getStylesheets().add(getClass().getResource("/css/TaskManage.css").toExternalForm());
            dialogPane.getStyleClass().add("custom-dialog");


        });
    }
    /**
     * 작업 목록을 업데이트하는 메서드
     * 작업 목록이 비어 있고 실제 작업이 존재하는 경우에만 목록을 업데이트
     */
    private void updateProjectList() {
        // projectListView가 null이면 초기화
        if (projectListView == null) {
            projectListView = new ListView<>();
        }

        // 프로젝트 목록이 비어 있고, 실제 프로젝트가 존재하는 경우에만 목록을 업데이트
        if (projectListView.getItems().isEmpty() && !projects.isEmpty()) {
            projectListView.setItems(projects);
        }
    }

    /**
     * 입력된 프로젝트 이름이 이미 존재하는지 검사하는 메서드
     *
     * @param projectName 검사할 프로젝트 이름
     * @return 중복 여부 (true: 중복됨, false: 중복되지 않음)
     */
    private boolean isProjectNameExist(String projectName) {
        for (ProjectManager project : projects) {
            if (project.getName().equals(projectName)) {
                return true;
            }
        }
        return false;
    }

}
class ProjectManager {
    private String projectName;
    private ObservableList<String> tasks;



    public ProjectManager(String name) {
        this.projectName = name;
        this.tasks = FXCollections.observableArrayList();
    }
    public void updateTask(String oldTask, String newTask) {
        int taskIndex = tasks.indexOf(oldTask);
        if (taskIndex != -1) {
            tasks.set(taskIndex, newTask);
        }
    }
    public void deleteTask(String task) {
        tasks.remove(task);
    }
    public String getName() {
        return projectName;
    }

    public ObservableList<String> getTasks() {
        return tasks;
    }

    @Override
    public String toString() {
        return projectName; // ListView에 프로젝트의 이름을 표시
    }

    public void addTask(String task) {
        tasks.add(task);
    }
    // 나머지 필요한 메서드...
}
