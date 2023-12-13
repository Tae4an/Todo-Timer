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
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

public class TodoTaskController implements Initializable {

    @FXML
    private ListView<String> taskListView; // 작업 목록을 표시하는 ListView 컴포넌트

    @FXML
    private Button tskmanage_btn; // "작업 관리" 기능을 위한 버튼
    @FXML
    private Button back_btn;
    @FXML
    private StackPane task_layout;
    @FXML
    private Label task_label;

    // 작업 목록을 관리하는 ObservableList, UI와 데이터의 동기화를 위해 사용
    private static ObservableList<String> tasks = FXCollections.observableArrayList();

    // 각 작업에 대한 마감일을 저장하는 Map, 키는 작업 이름, 값은 해당 작업의 마감일
    protected static final Map<String, LocalDate> dueDates = new HashMap<>();

    // 작업에 대한 메모를 저장하는 Map
    protected static final Map<String, String> taskMemos = new HashMap<>();

    // TodoTaskManageController 인스턴스, 작업 관리 화면의 컨트롤러
    private TodoTaskManageController manageController;

    // TodoTaskController 클래스의 싱글톤 인스턴스
    private static TodoTaskController instance;

    private static ObservableList<String> currentTasks; // 현재 선택된 프로젝트의 작업 목록

    private static boolean isInitialized = false;

    private static ProjectManager currentProject;




    /**
     * Singleton 패턴을 사용하여 TodoTaskController의 인스턴스를 반환
     *
     * @return TodoTaskController의 인스턴스
     */
    public static TodoTaskController getInstance() {
        if (instance == null) {
            instance = new TodoTaskController();
        }
        return instance;
    }

    /**
     * TodoTaskController의 생성자
     */
    public TodoTaskController() {
    }


    /**
     * FXML 파일이 로드될 때 자동으로 호출되는 초기화 메서드
     * UI 요소들의 초기 설정 및 이벤트 핸들러를 등록하는 역할을 수행
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {


        // "작업 관리" 버튼에 대한 클릭 이벤트 핸들러 설정
        tskmanage_btn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                // ListView에서 선택된 작업을 얻음
                String selectedTask = taskListView.getSelectionModel().getSelectedItem();

                // 작업이 없거나 선택되지 않았을 경우 에러 메시지 표시
                if (taskListView.getItems().isEmpty()) {
                    showPopup("Error", "작업이 없습니다..!");
                } else if (selectedTask == null) {
                    showPopup("Error", "작업을 선택하세요..!");
                } else {
                    // 선택된 작업으로 TodoTaskManageController 설정 및 뷰 로드
                    manageController = TodoTaskManageController.getInstance();
                    manageController.setSelectTask(selectedTask);
                    try {
                        Parent sub = FXMLLoader.load(getClass().getResource("TodoTaskManage.fxml"));
                        StackPane root = (StackPane) tskmanage_btn.getScene().getRoot();
                        root.getChildren().add(sub);

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
        back_btn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                try {
                    // TodoTask.fxml 파일을 로드하여 새로운 씬을 생성
                    Parent todoTaskScene = FXMLLoader.load(getClass().getResource("TodoMain.fxml"));
                    StackPane root = (StackPane) task_layout.getScene().getRoot();

                    // 현재 씬에 새로운 TodoTask 씬 추가
                    root.getChildren().add(todoTaskScene);

                    // 필요한 경우, 새 씬에 애니메이션 효과 적용
                    todoTaskScene.setTranslateX(-340); // 씬의 너비에 맞게 조정
                    Timeline timeline = new Timeline();
                    KeyValue keyValue = new KeyValue(todoTaskScene.translateXProperty(), 0);
                    KeyFrame keyFrame = new KeyFrame(Duration.millis(300), keyValue);
                    timeline.getKeyFrames().add(keyFrame);
                    timeline.play();

                    // 이전 씬 제거
                    root.getChildren().remove(1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        // 메서드가 처음 호출될 때만 currentTasks를 초기화
        if (!isInitialized) {
            this.currentTasks = FXCollections.observableArrayList();
            isInitialized = true;
        }
        taskListView.setItems(currentTasks); // ListView에 현재 작업 목록을 설정

        // 작업 목록 업데이트
        updateTaskList();
    }


    /**
     * 작업을 추가하는 메서드
     */
    @FXML
    private void addTask() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("작업 추가");
        dialog.setHeaderText("새로운 작업을 추가하세요");

        // style.css 파일의 URL을 안전하게 가져오기
        URL cssUrl = getClass().getResource("/style.css"); // 절대 경로 사용
        if (cssUrl != null) {
            dialog.getDialogPane().getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.out.println("style.css 파일을 찾을 수 없음");
        }

        dialog.setContentText("작업 이름:");
        dialog.getDialogPane().setStyle("-fx-background-color: #ffb66e;");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(taskName -> {
            if (!taskName.isEmpty() && currentProject != null) {
                currentProject.addTask(taskName); // 프로젝트에 작업 추가
                currentTasks.add(taskName);
                tasks.add(taskName); // 현재 작업 목록 업데이트

                taskListView.setItems(currentTasks);
            }
        });
    }


    /**
     * 선택한 작업을 삭제하기 위한 메서드
     *
     * @param selectedTask 삭제할 작업
     */
    public void deleteTask(String selectedTask) {
        if (tasks.remove(selectedTask)) {
            currentTasks.remove(selectedTask); // currentTasks에서도 삭제
            updateTaskList();
        }
    }

    /**
     * 작업을 업데이트하는 메서드
     *
     * @param oldTask 이전 작업 이름
     * @param newTask 새로운 작업 이름
     */
    public void updateTask(String oldTask, String newTask) {
        int index = tasks.indexOf(oldTask);
        if (index != -1) {
            tasks.set(index, newTask); // tasks에서 업데이트
            int currentTaskIndex = currentTasks.indexOf(oldTask);
            if (currentTaskIndex != -1) {
                currentTasks.set(currentTaskIndex, newTask); // currentTasks에서도 업데이트
            }
        }
    }

    /**
     * 지정된 작업의 마감일을 업데이트하는 메서드.
     *
     * @param task    업데이트할 작업의 이름
     * @param dueDate 새로운 마감일
     */
    public void updateDueDate(String task, LocalDate dueDate) {
        // 'dueDates' 맵에 작업 이름을 키로 하고 마감일을 값으로 저장
        dueDates.put(task, dueDate);
    }

    /**
     * 지정된 작업의 마감일을 반환하는 메서드.
     *
     * @param task 조회할 작업의 이름
     * @return 해당 작업의 마감일, 저장된 마감일이 없을 경우 null 반환
     */
    public LocalDate getDueDate(String task) {
        // 'dueDates' 맵에서 작업 이름에 해당하는 마감일을 조회하여 반환
        return dueDates.get(task);
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
        });
    }


    /**
     * 작업 목록을 업데이트하는 메서드
     * 작업 목록이 비어 있고 실제 작업이 존재하는 경우에만 목록을 업데이트
     */
    private void updateTaskList() {
        // taskListView가 null이면 초기화
        if (taskListView == null) {
            taskListView = new ListView<>();
        }

        // 작업 목록이 비어 있고, 실제 작업이 존재하는 경우에만 목록을 업데이트
        if (taskListView.getItems().isEmpty() && !tasks.isEmpty()) {
            taskListView.setItems(currentTasks); // ListView에 현재 작업 목록을 설정
        }
    }

    /**
     * 작업 목록을 반환
     *
     * @return 작업 목록
     */
    public ObservableList<String> getTasks() {
        return tasks;
    }

    public void setCurrentProject(ProjectManager project) {
        this.currentProject = project;
        this.currentTasks.setAll(project.getTasks());


        if (this.taskListView != null) {
            taskListView.setItems(this.currentTasks);
        }
    }

    public void updateTaskMemo(String task, String memo) {
        taskMemos.put(task, memo);
    }

    public String getTaskMemo(String task) {
        return taskMemos.getOrDefault(task, "");
    }
}