package com.example.todo_timer;

import javafx.animation.*;
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
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

import static com.example.todo_timer.TodoTaskController.dueDates;
import static com.example.todo_timer.TodoTaskController.taskMemos;


public class TodoMainController implements Initializable {
    @FXML
    private Button tm_btn; // "타이머" 버튼
    @FXML
    private Button addProject_btn; // "프로젝트 추가" 버튼
    @FXML
    private ListView<ProjectManager> projectListView; // 프로젝트 목록을 표시하는 ListView
    @FXML
    private Button manageTask_btn; // "To-Do 관리" 버튼
    @FXML
    private Button manageProject_btn; // "프로젝트 관리" 버튼

    // 프로젝트 관리 목록을 저장하는 ObservableList
    private static ObservableList<ProjectManager> projects = FXCollections.observableArrayList();

    // TodoTaskController 인스턴스
    private final TodoTaskController todoTaskController;


    /**
     * TodoMainController의 생성자
     */
    public TodoMainController() {
        // TodoTaskController 인스턴스 생성
        todoTaskController = new TodoTaskController();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // "타이머" 버튼에 대한 클릭 이벤트 핸들러
        tm_btn.setOnMouseClicked(event -> {
            try {
                // TodoTimer.fxml 뷰를 로드하고 화면에 추가
                Parent sub = FXMLLoader.load(getClass().getResource("TodoTimer.fxml"));
                StackPane root = (StackPane) tm_btn.getScene().getRoot();
                root.getChildren().add(sub);

                // 뷰에 줌 인 애니메이션 효과 적용
                sub.setScaleX(0); // 초기 스케일을 0으로 설정 (완전히 작게 시작)
                sub.setScaleY(0); // 초기 스케일을 0으로 설정 (완전히 작게 시작)

                ScaleTransition zoomIn = new ScaleTransition(Duration.millis(300), sub);
                zoomIn.setToX(1); // 최종적으로 정상 크기로
                zoomIn.setToY(1); // 최종적으로 정상 크기로

                zoomIn.play(); // 애니메이션 실행
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        // "To-Do 관리" 버튼에 대한 클릭 이벤트 핸들러
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
                    todoTaskController.setCurrentProject(selectedProject);
                    Parent sub = FXMLLoader.load(getClass().getResource("TodoTask.fxml"));
                    StackPane root = (StackPane) manageTask_btn.getScene().getRoot();
                    root.getChildren().add(sub);

                    // 뷰에 오른쪽 바깥에서 중앙으로 슬라이드하는 애니메이션 효과 적용
                    sub.setTranslateX(root.getWidth()); // 시작 위치를 화면 오른쪽 바깥으로 설정
                    TranslateTransition slideTransition = new TranslateTransition(Duration.millis(300), sub);
                    slideTransition.setToX(0); // 최종 위치를 화면 내부(0)로 설정

                    slideTransition.play(); // 애니메이션 실행

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });


        /*
          projectListView의 셀 팩토리를 설정하는 메서드

          @param lv ListView<ProjectManager> 인스턴스
         */
        projectListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(ProjectManager project, boolean empty) {
                super.updateItem(project, empty);
                if (empty || project == null) {
                    setText(null);
                } else {
                    setText(project.toString()); // 여기서 프로젝트 이름과 작업 수를 표시
                }
            }
        });

        updateProjectList(); // 프로젝트 목록 업데이트
        checkAllDeadlines(); // 마감 기한 체크
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

            //팝업창 색감 스타일 입히기
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
     * 입력된 프로젝트 이름이 이미 존재 하는지 검사 하는 메서드
     *
     * @param projectName 검사할 프로젝트 이름
     * @return 중복 여부 (true: 중복됨, false: 중복 되지 않음)
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
     * 프로젝트 이름을 편집 하는 메서드
     *
     * @param project 편집할 프로젝트
     */
    private void editProjectName(ProjectManager project) {
        TextInputDialog dialog = new TextInputDialog(project.getName());
        dialog.setTitle("프로젝트 이름 수정");
        dialog.setHeaderText("새 프로젝트 이름을 입력하세요.");
        // 다이얼로그 패널에 접근 >> 신창영
        DialogPane dialogPane = dialog.getDialogPane();

        dialogPane.getStylesheets().add(getClass().getResource("/css/TodoTimer.css").toExternalForm());
        dialogPane.getStyleClass().add("custom-dialog");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newName -> {
            // 중복 프로젝트 이름을 확인
            if (isProjectNameExist(newName)) {
                showPopup("중복된 프로젝트", "이미 존재하는 프로젝트 이름입니다.");
            } else {
                String oldName = project.getName();
                project.setName(newName);
                showPopup("수정", "수정 되었습니다..!");
                updateProjectNameInDueDatesAndMemos(oldName, newName);
                updateProjectList();
                reloadMainScene();
            }
        });
    }

    /**
     * dueDates와 taskMemos 맵에서 프로젝트 이름을 변경 하는 메서드
     *
     * @param oldName 이전 프로젝트 이름
     * @param newName 새로운 프로젝트 이름
     */
    private void updateProjectNameInDueDatesAndMemos(String oldName, String newName) {
        // dueDates 맵 업데이트
        Map<String, LocalDate> updatedDueDates = new HashMap<>();
        dueDates.forEach((key, value) -> {
            // 프로젝트 이름이 이전 프로젝트 이름으로 시작하는 경우
            if (key.startsWith(oldName + " - ")) {
                // 새로운 프로젝트 이름을 추가한 키로 변경
                key = newName + key.substring(oldName.length());
            }
            // 업데이트된 키와 값을 새 맵에 저장
            updatedDueDates.put(key, value);
        });
        // 원래 맵을 비우고 업데이트된 맵의 내용을 복사
        dueDates.clear();
        dueDates.putAll(updatedDueDates);

        // taskMemos 맵 업데이트
        Map<String, String> updatedTaskMemos = new HashMap<>();
        taskMemos.forEach((key, value) -> {
            // 프로젝트 이름이 이전 프로젝트 이름으로 시작하는 경우
            if (key.startsWith(oldName + " - ")) {
                // 새로운 프로젝트 이름을 추가한 키로 변경
                key = newName + key.substring(oldName.length());
            }
            // 업데이트된 키와 값을 새 맵에 저장
            updatedTaskMemos.put(key, value);
        });
        // 원래 맵을 비우고 업데이트된 맵의 내용을 복사
        taskMemos.clear();
        taskMemos.putAll(updatedTaskMemos);
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

        // 다이얼로그 패널에 접근
        DialogPane dialogPane = confirmation.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/css/TodoTimer.css").toExternalForm());
        dialogPane.getStyleClass().add("custom-dialog");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // 사용자가 '확인'을 선택한 경우

            // allTasks에서 해당 프로젝트의 작업들을 제거
            ProjectManager.getAllTasks().removeIf(task -> task.getProjectName().equals(project.getName()));

            // ObservableList에서 프로젝트 제거
            projects.remove(project);

            // 사용자에게 피드백 제공
            showPopup("삭제", "삭제 되었습니다..!");

            // 프로젝트 목록 업데이트
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

    /**
     * 모든 프로젝트의 작업들의 마감 기한을 확인하고, 마감일에 따라 팝업 메시지를 표시하는 메서드
     */
    private void checkAllDeadlines() {
        LocalDate today = LocalDate.now();

        // 모든 프로젝트를 반복하며 작업들의 마감 기한 확인
        for (ProjectManager project : projects) {
            for (String task : project.getTasks()) {
                // 작업의 마감일을 가져옴
                LocalDate dueDate = todoTaskController.getDueDate(todoTaskController.getCurrentProjectName(), task);

                if (dueDate != null) {
                    if (dueDate.isEqual(today.plusDays(1))) {
                        // 마감일이 하루 남았을 경우
                        String message = String.format("작업 '%s'의 마감 기한이 하루 남았습니다.", task);
                        Platform.runLater(() -> showPopup("마감 임박", message));
                    } else if (dueDate.isEqual(today)) {
                        // 마감일이 오늘인 경우
                        String message = String.format("작업 '%s'의 마감 기한이 오늘입니다!", task);
                        Platform.runLater(() -> showPopup("마감일", message));
                    }
                }
            }
        }
    }

    /**
     * 프로젝트 관리 다이얼로그를 표시하고, 사용자가 프로젝트 이름을 수정하거나 삭제할 수 있는 메서드
     */
    @FXML
    public void manageProject() {
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

        DialogPane dialogPane = alert.getDialogPane();

        dialogPane.getStylesheets().add(getClass().getResource("/css/TodoTimer.css").toExternalForm());
        dialogPane.getStyleClass().add("custom-dialog");

        ButtonType editButton = new ButtonType("수정"); // 수정 버튼 추가
        ButtonType deleteButton = new ButtonType("삭제"); // 삭제 버튼 추가
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
    }

    /**
     * 사용자에게 프로젝트 이름을 입력받아 새로운 프로젝트를 추가하는 메서드
     */
    @FXML
    public void addProject() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("프로젝트 추가");
        dialog.setHeaderText("새 프로젝트의 이름을 입력하세요");

        // 다이얼로그 패널에 접근
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
    }


}



