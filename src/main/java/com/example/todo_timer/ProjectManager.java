package com.example.todo_timer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * 프로젝트 관리를 위한 클래스
 * 이 클래스는 프로젝트 이름과 해당 프로젝트에 속한 작업 목록을 관리
 */
public class ProjectManager {
    private String projectName;    // 각 프로젝트 이름
    private ObservableList<String> tasks; // 각 프로젝트에 속한 작업 목록

    private static ObservableList<Task> allTasks = FXCollections.observableArrayList(); // 모든 프로젝트에 속한 작업

    private ObservableList<String> completedTasks; // 각 프로젝트에 속한 완료한 작업 목록

    /**
     * ProjectManager의 생성자
     */
    public ProjectManager() {
    }

    /**
     * ProjectManager의 생성자
     *
     * @param name 프로젝트의 이름
     */
    public ProjectManager(String name) {
        this.projectName = name;
        this.tasks = FXCollections.observableArrayList();
        this.completedTasks = FXCollections.observableArrayList();
    }

    /**
     * 해당 프로젝트에 속한 작업의 수를 반환하는 메서드
     *
     * @return 현재 프로젝트에 속한 작업의 수를 나타내는 정수 값
     */
    public int getNumberOfTasks() {
        // 현재 프로젝트에 속한 작업의 수를 반환
        return tasks.size();
    }


    /**
     * 객체를 문자열로 나타내는 메서드
     *
     * @return 프로젝트 이름과 해당 프로젝트에 속한 작업 수를 문자열로 반환
     */
    @Override
    public String toString() {
        return projectName + " (" + getNumberOfTasks() + ")";
    }

    /**
     * 작업 이름을 업데이트. 기존 작업 이름을 새로운 이름으로 변경
     *
     * @param oldTask 이전 작업 이름
     * @param newTask 새 작업 이름
     */
    public void updateTask(String oldTask, String newTask) {
        int taskIndex = tasks.indexOf(oldTask);
        if (taskIndex != -1) {
            tasks.set(taskIndex, newTask);

            // allTasks 목록에서 해당 작업을 찾아 업데이트
            for (Task task : allTasks) {
                if (task.getName().equals(oldTask) && task.getProjectName().equals(projectName)) {
                    // Task 객체의 생성자와 멤버 변수를 수정해야 할 수도 있음
                    allTasks.set(allTasks.indexOf(task), new Task(newTask, projectName));
                    break; // 일치하는 작업을 찾으면 루프 종료
                }
            }
        }
    }


    /**
     * 지정된 작업을 삭제
     *
     * @param task 삭제할 작업 이름
     */
    public void deleteTask(String task) {
        tasks.remove(task);
        allTasks.removeIf(t -> t.getName().equals(task) && t.getProjectName().equals(projectName));
    }

    /**
     * 완료된 작업을 삭제하는 메서드
     *
     * @param task 삭제할 완료된 작업의 이름
     */
    public void deleteCompletedTask(String task) {
        completedTasks.remove(task);
    }

    /**
     * 프로젝트의 이름을 반환
     *
     * @return 프로젝트 이름
     */
    public String getName() {
        return projectName;
    }


    /**
     * 프로젝트에 속한 작업 목록을 반환
     *
     * @return 작업 목록
     */
    public ObservableList<String> getTasks() {
        return tasks;
    }


    /**
     * 완료된 작업 목록을 반환하는 메서드
     *
     * @return 완료된 작업 목록
     */
    public ObservableList<String> getCompletedTasks() {
        return completedTasks;
    }


    /**
     * 새로운 작업을 프로젝트에 추가
     *
     * @param task 추가할 작업 이름
     */
    public void addTask(String task) {
        tasks.add(task);
        allTasks.add(new Task(task, projectName)); // Task 객체 생성
    }
    /**
     * 완료된 작업을 추가하는 메서드
     *
     * @param task 추가할 완료된 작업
     */
    public void addCompletedTask(String task) {
        completedTasks.add(task);
    }

    /**
     * 모든 작업 목록을 반환하는 메서드
     *
     * @return 모든 작업 목록
     */
    public static ObservableList<Task> getAllTasks(){
        return allTasks;
    }


    /**
     * 프로젝트 이름을 수정
     *
     * @param newName 새로운 프로젝트 이름
     */
    public void setName(String newName) {
        // allTasks 목록에서 해당 프로젝트의 작업을 찾아 프로젝트 이름을 업데이트
        for (Task task : allTasks) {
            if (task.getProjectName().equals(this.projectName)) {
                task.setProjectName(newName);
            }
        }
        // 프로젝트 이름을 업데이트
        this.projectName = newName;
    }
}