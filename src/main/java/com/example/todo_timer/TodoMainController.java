package com.example.todo_timer;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
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
    private Button manageTask_btn; // "To-Do 관리" 버튼
    @FXML
    private Button manageProject_btn; // "프로젝트 관리" 버튼

    private static ObservableList<ProjectManager> projects = FXCollections.observableArrayList();
    private final TodoTaskController todoTaskController = TodoTaskController.getInstance();


    public TodoMainController() {
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // "프로젝트 추가" 버튼에 대한 클릭 이벤트 핸들러
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

        // "타이머" 버튼에 대한 클릭 이벤트 핸들러
        tm_btn.setOnMouseClicked(event -> {
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
        });

        // "프로젝트 관리" 버튼에 대한 클릭 이벤트 핸들러
        manageProject_btn.setOnMouseClicked(event -> {
            // ListView에서 선택된 프로젝트를 얻어옴
            ProjectManager selectedProject = projectListView.getSelectionModel().getSelectedItem();

            // 프로젝트가 선택되지 않았을 경우 에러 메시지를 표시
            if (selectedProject == null) {
                showPopup("오류", "프로젝트를 선택하세요..!");
                return;
            }

            // 사용자에게 프로젝트 관리 옵션을 선택할 다이얼로그를 표시
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("프로젝트 관리");
            alert.setHeaderText("프로젝트 관리 옵션을 선택하세요.");
            alert.setContentText("프로젝트 이름을 수정하거나 삭제할 수 있습니다.");
            alert.getDialogPane().setStyle("-fx-background-color:  #f8d8ca;");

            ButtonType editButton = new ButtonType("수정");
            ButtonType deleteButton = new ButtonType("삭제");
            alert.getButtonTypes().setAll(editButton, deleteButton, ButtonType.CANCEL); // 취소 버튼 추가

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent()) {
                if (result.get() == editButton) {
                    // "프로젝트 이름 수정" 버튼이 선택된 경우
                    editProjectName(selectedProject);
                } else if (result.get() == deleteButton) {
                    // "프로젝트 삭제" 버튼이 선택된 경우
                    deleteProject(selectedProject);
                }
            }
        });

        // "작업 관리" 버튼에 대한 클릭 이벤트 핸들러
        manageTask_btn.setOnMouseClicked(event -> {
            // ListView에서 선택된 작업을 얻어옴
            ProjectManager selectedProject = projectListView.getSelectionModel().getSelectedItem();

            // 작업이 없거나 선택되지 않았을 경우 에러 메시지를 표시
            if (projectListView.getItems().isEmpty()) {
                showPopup("오류", "프로젝트가 없습니다..!");
            } else if (selectedProject == null) {
                showPopup("오류", "프로젝트를 선택하세요..!");
            } else {
                try {
                    Parent sub = FXMLLoader.load(getClass().getResource("TodoTask.fxml"));
                    StackPane root = (StackPane) manageTask_btn.getScene().getRoot();
                    root.getChildren().add(sub);
                    todoTaskController.setCurrentProject(selectedProject);

                    // 뷰에 애니메이션 효과를 적용합니다.
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
        });


        projectListView.setItems(projects);
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

            //팝업창 색감 스타일 입히지 : 신창영
            alert.getDialogPane().getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            alert.showAndWait();

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
        projectListView.setItems(projects);

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


    /**
     * 프로젝트 이름을 편집하는 메서드
     *
     * @param project 편집할 프로젝트
     */
    private void editProjectName(ProjectManager project) {
        TextInputDialog dialog = new TextInputDialog(project.getName());
        dialog.setTitle("프로젝트 이름 수정");
        dialog.setHeaderText("새 프로젝트 이름을 입력하세요:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newName -> {
            // 중복 프로젝트 이름을 확인
            if (isProjectNameExist(newName)) {
                showPopup("중복된 프로젝트", "이미 존재하는 프로젝트 이름입니다.");
            } else {
                project.setName(newName);
                showPopup("수정", "수정 되었습니다..!");
                updateProjectList();
                reloadMainScene();
            }
        });
    }

    /**
     * 프로젝트를 삭제하는 메서드
     *
     * @param project 삭제할 프로젝트
     */
    private void deleteProject(ProjectManager project) {
        // 프로젝트 삭제를 위한 확인 메시지를 표시
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("프로젝트 삭제");
        confirmation.setHeaderText("프로젝트 삭제 확인");
        confirmation.setContentText("선택한 프로젝트 '" + project + "'를 삭제하시겠습니까?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            projects.remove(project);
            showPopup("삭제", "삭제 되었습니다..!");
            updateProjectList();
            reloadMainScene();
        }
    }

    /**
     * 메인 씬을 다시 불러오는 메서드
     */
    private void reloadMainScene() {
        try {
            // 메인 씬을 다시 로드
            FXMLLoader loader = new FXMLLoader(getClass().getResource("TodoMain.fxml"));
            Parent mainScene = loader.load();

            // 현재 스테이지를 얻어옴
            Stage stage = (Stage) tm_btn.getScene().getWindow();

            // 새로운 씬을 설정
            Scene scene = new Scene(mainScene);
            stage.setScene(scene);

            // 컨트롤러 초기화
            TodoMainController mainController = loader.getController();
            mainController.initialize(null, null); // 필요에 따라 초기화 파라미터 설정

            // 씬을 보여줌
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}



