package com.example.todo_timer;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
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
import java.util.stream.Collectors;

public class TodoTaskController implements Initializable {

    @FXML
    private ListView<String> taskListView; // 작업 목록을 표시하는 ListView 컴포넌트
    @FXML
    private ListView<String> completedTaskListView; // 완료한 작업 목록을 표시하는 ListView 컴포넌트
    @FXML
    private Button tskmanage_btn; // "작업 관리" 기능을 위한 버튼
    @FXML
    private Button back_btn;       // 뒤로 가기 버튼
    @FXML
    private StackPane task_layout; // 작업 목록을 표시하는 레이아웃
    @FXML
    private Label task_label;      // 작업 레이블
    @FXML
    private Button complete_btn;   // 작업 완료 버튼
    @FXML
    private Button restore_btn;    // 작업 복구 버튼

    // 작업 목록을 관리하는 ObservableList
    private static ObservableList<String> tasks;
    // 완료한 작업 목록을 관리하는 ObservableList
    private static ObservableList<String> completedTasks = FXCollections.observableArrayList();

    // 각 작업에 대한 마감일을 저장하는 Map, 키는 작업 이름, 값은 해당 작업의 마감일
    protected static final Map<String, LocalDate> dueDates = new HashMap<>();

    // 작업에 대한 메모를 저장하는 Map
    protected static final Map<String, String> taskMemos = new HashMap<>();

    // TodoTaskManageController 인스턴스, 작업 관리 화면의 컨트롤러
    private TodoTaskManageController manageController;

    private static boolean isInitialized = false; // 초기화 여부를 나타내는 플래그

    private static ProjectManager projects; // 현재 프로젝트


    /**
     * TodoTaskController의 생성자
     */
    public TodoTaskController() {
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // "작업 관리" 버튼에 대한 클릭 이벤트 핸들러 설정
        tskmanage_btn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                String selectedTask = taskListView.getSelectionModel().getSelectedItem();
                String selectedCompletedTask = completedTaskListView.getSelectionModel().getSelectedItem();
                selectedTask = extractTaskName(selectedTask);

                selectedCompletedTask = extractTaskName(selectedCompletedTask);
                // 작업이 없거나 선택되지 않았을 경우 에러 메시지 표시
                if (taskListView.getItems().isEmpty()) {
                    showPopup("Error", "작업이 없습니다..!");
                } else if (selectedTask == null && selectedCompletedTask == null) {
                    showPopup("Error", "작업을 선택하세요..!");
                } else if (selectedCompletedTask != null) {
                    // 완료한 작업은 변경할 수 없다는 팝업 표시
                    showPopup("Error", "완료한 작업은 변경할 수 없습니다.");
                } else {
                    // 선택된 작업으로 TodoTaskManageController 설정 및 뷰 로드
                    manageController = new TodoTaskManageController();
                    manageController.setSelectTask(selectedTask);
                    try {
                        Parent sub = FXMLLoader.load(getClass().getResource("TodoTaskManage.fxml"));
                        StackPane root = (StackPane) tskmanage_btn.getScene().getRoot();
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
            }
        });

        // "뒤로 가기" 버튼에 대한 클릭 이벤트 핸들러
        back_btn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                try {
                    // TodoMain.fxml 파일을 로드하여 새로운 씬을 생성
                    Parent todoMainScene = FXMLLoader.load(getClass().getResource("TodoMain.fxml"));
                    StackPane root = (StackPane) task_layout.getScene().getRoot();
                    Parent currentScene = (Parent) root.getChildren().get(0);

                    // 새 씬 추가
                    root.getChildren().add(todoMainScene);
                    todoMainScene.setOpacity(0); // 새 씬을 투명하게 시작

                    // 현재 씬에 페이드 아웃 애니메이션 적용
                    FadeTransition fadeOut = new FadeTransition(Duration.millis(300), currentScene);
                    fadeOut.setFromValue(1);
                    fadeOut.setToValue(0);

                    // 새 씬에 페이드 인 애니메이션 적용
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(300), todoMainScene);
                    fadeIn.setFromValue(0);
                    fadeIn.setToValue(1);

                    // 두 애니메이션 동시 실행
                    fadeOut.play();
                    fadeIn.play();

                    // 페이드 아웃 종료 후 이전 씬 제거
                    fadeOut.setOnFinished(e -> root.getChildren().remove(currentScene));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


        // 메서드가 처음 호출될 때만 currentTasks를 초기화
        if (!isInitialized) {
            this.tasks = FXCollections.observableArrayList();
            this.completedTasks = FXCollections.observableArrayList();
            isInitialized = true;
        }
        // 작업 목록 업데이트
        updateTaskList();
        task_label.setText(projects.getName());
    }


    /**
     * 작업을 추가하는 메서드
     */
    @FXML
    private void addTask() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("작업 추가");
        dialog.setHeaderText("새로운 작업을 추가하세요 ");

        // 다이얼로그 패널에 접근
        DialogPane dialogPane = dialog.getDialogPane();

        dialogPane.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        dialogPane.getStyleClass().add("custom-dialog");

        // style.css 파일의 URL을 안전하게 가져옴
        URL cssUrl = getClass().getResource("/style.css"); // 절대 경로 사용
        if (cssUrl != null) {
            dialog.getDialogPane().getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.out.println("style.css 파일을 찾을 수 없음");
        }

        dialog.setContentText("작업 이름:");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(taskName -> {
            if (!taskName.isEmpty() && projects != null) {
                if (isTaskNameExist(taskName)) {
                    showPopup("중복된 작업", "이미 존재하는 작업 이름입니다.");
                } else {
                    projects.addTask(taskName); // 프로젝트에 작업 추가
                    tasks.add(taskName);
                    updateTaskList();
                }
            }
        });

    }


    /**
     * 선택한 작업을 삭제하기 위한 메서드
     *
     * @param selectedTask 삭제할 작업
     */
    public void deleteTask(String selectedTask) {
        selectedTask = extractTaskName(selectedTask);
        tasks.remove(selectedTask);
        updateTaskList();

        if (projects != null) {
            projects.deleteTask(selectedTask); // 프로젝트의 작업 목록에서 삭제
        }
    }

    /**
     * 작업을 업데이트하는 메서드
     *
     * @param oldTask 이전 작업 이름
     * @param newTask 새로운 작업 이름
     */
    public void updateTask(String oldTask, String newTask) {
        if (projects != null && projects.getTasks().contains(oldTask)) {
            // 현재 프로젝트에서 oldTask 이름의 작업을 찾아 newTask로 업데이트
            projects.updateTask(oldTask, newTask);

            // 전역 작업 목록(tasks)과 현재 작업 목록(tasks) 업데이트
            updateGlobalAndCurrentTaskLists(oldTask, newTask);
        }
    }

    /**
     * 전역 및 현재 작업 목록을 업데이트하는 메서드
     *
     * @param oldTask 이전 작업 이름
     * @param newTask 새로운 작업 이름
     */
    private void updateGlobalAndCurrentTaskLists(String oldTask, String newTask) {
        // taskListView가 null이면 초기화
        if (taskListView == null) {
            taskListView = new ListView<>();
        }
        int index = tasks.indexOf(oldTask);
        if (index != -1) {
            tasks.set(index, newTask);
        }

        taskListView.setItems(tasks); // ListView 업데이트

    }


    /**
     * 지정된 작업의 마감일을 업데이트하는 메서드.
     *
     * @param projectName 프로젝트 이름
     * @param taskName    작업 이름
     * @param dueDate     새로운 마감일
     */
    public void updateDueDate(String projectName, String taskName, LocalDate dueDate) {
        String key = projectName + " - " + taskName;
        dueDates.put(key, dueDate);
    }

    /**
     * 지정된 작업의 마감일을 반환하는 메서드.
     *
     * @param projectName 프로젝트 이름
     * @param taskName    작업 이름
     * @return 해당 작업의 마감일, 저장된 마감일이 없을 경우 null 반환
     */
    public LocalDate getDueDate(String projectName, String taskName) {
        String key = projectName + " - " + taskName;
        return dueDates.get(key);
    }



    /**
     * 작업 목록과 완료한 작업 목록을 업데이트하는 메서드
     */
    private void updateTaskList() {
        // taskListView 및 completedTaskListView가 null이면 초기화
        if (taskListView == null) {
            taskListView = new ListView<>();
        }
        if (completedTaskListView == null) {
            completedTaskListView = new ListView<>();
        }

        // 현재 작업 목록을 가공하여 포맷팅
        ObservableList<String> formattedTasks = tasks.stream()
                .map(this::formatTaskWithDueDate)
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        taskListView.setItems(formattedTasks); // ListView에 현재 작업 목록 설정

        // 완료한 작업 목록을 가공하여 포맷팅
        ObservableList<String> formattedCompletedTasks = completedTasks.stream()
                .map(this::formatTaskWithDueDate)
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        completedTaskListView.setItems(formattedCompletedTasks); // ListView에 완료한 작업 목록 설정
    }



    /**
     * 작업 이름에 마감일 정보를 포함하여 포맷하는 메서드
     *
     * @param task 작업 이름
     * @return 포맷된 작업 이름 (마감일 정보가 포함될 수 있음)
     */
    private String formatTaskWithDueDate(String task) {
        String projectName = projects != null ? projects.getName() : "";
        String key = projectName + " - " + task;
        LocalDate dueDate = dueDates.get(key);

        if (dueDate != null) {
            return task + " [" + dueDate.toString() + "]";
        } else {
            return task;
        }
    }

    /**
     * 현재 프로젝트를 설정하고 해당 프로젝트의 작업 목록과 완료된 작업 목록을 가져와 설정하는 메서드
     *
     * @param project 현재 프로젝트
     */
    public void setCurrentProject(ProjectManager project) {
        // 작업 목록과 완료된 작업 목록을 초기화
        if (tasks == null) {
            tasks = FXCollections.observableArrayList();
        }
        if (completedTasks == null) {
            completedTasks = FXCollections.observableArrayList();
        }

        // 현재 프로젝트 설정
        projects = project;

        // 현재 프로젝트의 작업 목록과 완료된 작업 목록을 가져와 설정
        tasks.setAll(project.getTasks());
        completedTasks.setAll(project.getCompletedTasks());

        // 작업 목록과 완료된 작업 목록을 ListView에 설정
        if (this.taskListView != null) {
            taskListView.setItems(tasks);
        }
        if (this.completedTaskListView != null) {
            completedTaskListView.setItems(completedTasks);
        }
    }


    /**
     * 작업 메모를 업데이트하는 메서드.
     * 프로젝트 이름과 작업 이름을 결합하여 고유한 키 생성.
     *
     * @param projectName 프로젝트 이름
     * @param taskName 작업 이름
     * @param memo 작업 메모
     */
    public void updateTaskMemo(String projectName, String taskName, String memo) {
        String key = projectName + " - " + taskName;
        taskMemos.put(key, memo);
    }

    /**
     * 지정된 작업의 메모를 반환하는 메서드.
     * 프로젝트 이름과 작업 이름을 결합하여 고유한 키 생성.
     *
     * @param projectName 프로젝트 이름
     * @param taskName 작업 이름
     * @return 해당 작업의 메모, 저장된 메모가 없을 경우 빈 문자열 반환
     */
    public String getTaskMemo(String projectName, String taskName) {
        String key = projectName + " - " + taskName;
        return taskMemos.getOrDefault(key, "");
    }
    /**
     * 입력된 작업 이름이 이미 존재하는지 검사하는 메서드
     *
     * @param taskName 검사할 작업 이름
     * @return 중복 여부 (true: 중복됨, false: 중복되지 않음)
     */
    public boolean isTaskNameExist(String taskName) {
        return projects != null && projects.getTasks().contains(taskName);
    }

    /**
     * 선택된 작업을 완료 상태로 처리하는 이벤트 핸들러
     *
     * @param event 이벤트 객체
     */
    @FXML
    private void completeTask(ActionEvent event) {
        // ListView에서 선택된 작업을 얻음
        String selectedTaskWithDate = taskListView.getSelectionModel().getSelectedItem();

        // 완료한 작업이 선택되지 않았을 경우 에러 메시지 표시
        if (selectedTaskWithDate == null) {
            showPopup("Error", "완료한 작업을 선택하세요..!");
            return;
        }

        // 선택된 작업에서 작업 이름만 추출 (마감일 정보 제외)
        String selectedCompletedTask = extractTaskName(selectedTaskWithDate);

        // 완료된 작업 목록에 추가
        completedTasks.add(selectedCompletedTask);

        // 프로젝트 관리자에게 완료된 작업 추가
        projects.addCompletedTask(selectedCompletedTask);

        // 프로젝트 관리자에게 기존 작업 삭제
        projects.deleteTask(selectedCompletedTask);

        // 작업 목록에서 선택된 작업 제거
        tasks.remove(selectedCompletedTask);

        // 작업 목록 업데이트
        updateTaskList();
    }


    /**
     * 현재 프로젝트의 이름을 반환하는 메서드.
     *
     * @return 현재 프로젝트의 이름
     */
    public String getCurrentProjectName() {
        if (projects != null) {
            return projects.getName();
        } else {
            return ""; // 현재 프로젝트가 설정되지 않은 경우 빈 문자열 반환
        }
    }

    /**
     * 선택된 완료한 작업을 다시 작업 목록으로 복원하는 이벤트 핸들러
     *
     * @param event 이벤트 객체
     */
    @FXML
    private void restoreTask(ActionEvent event) {
        // ListView에서 선택된 완료한 작업을 얻음
        String selectedCompletedTaskWithDate = completedTaskListView.getSelectionModel().getSelectedItem();

        if (selectedCompletedTaskWithDate == null) {
            showPopup("Error", "완료한 작업을 선택하세요..!");
            return;
        }

        // 선택된 완료한 작업에서 작업 이름만 추출 (마감일 정보 제외)
        String selectedCompletedTask = extractTaskName(selectedCompletedTaskWithDate);

        // 선택된 완료한 작업을 다시 작업 목록으로 이동
        tasks.add(selectedCompletedTask);
        projects.addTask(selectedCompletedTask);
        projects.deleteCompletedTask(selectedCompletedTask);
        completedTasks.remove(selectedCompletedTask);

        // 작업 목록 업데이트
        updateTaskList();
    }


    /**
     * 작업 이름에서 마감일 정보를 제외한 문자열을 추출하는 메서드
     *
     * @param taskWithDate 작업 이름과 마감일 정보가 포함된 문자열
     * @return 작업 이름
     */
    private String extractTaskName(String taskWithDate) {
        if (taskWithDate != null) {
            // '[' 문자 앞의 문자열을 작업 이름으로 간주
            int bracketIndex = taskWithDate.indexOf(" [");
            return (bracketIndex != -1) ? taskWithDate.substring(0, bracketIndex) : taskWithDate;
        }
        return null;
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

}