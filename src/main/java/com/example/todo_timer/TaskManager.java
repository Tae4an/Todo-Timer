package com.example.todo_timer;

import javafx.collections.ObservableList;
import javafx.scene.control.ListView;

/**
 * TaskManager 클래스는 ListView에서 작업을 관리
 */
public class TaskManager {

    /**
     * 작업을 ListView에 추가
     *
     * @param taskListView 작업이 추가될 ListView
     * @param task         추가할 작업
     */
    public void addTask(ListView<String> taskListView, String task) {
        // 작업이 비어있지 않은지 확인한 후 추가
        if (!task.isEmpty()) {
            taskListView.getItems().add(task);
        }
    }

    /**
     * ListView에서 기존 작업을 수정
     *
     * @param taskListView 수정할 작업이 있는 ListView
     * @param index        수정할 작업의 인덱스
     * @param updatedTask  업데이트된 작업 내용
     */
    public void editTask(ListView<String> taskListView, int index, String updatedTask) {
        // 인덱스가 유효하고 업데이트된 작업이 비어있지 않은지 확인
        if (index >= 0 && index < taskListView.getItems().size() && !updatedTask.isEmpty()) {
            // taskListView에서 현재 표시되고 있는 항목들을 가져와서 ObservableList에 저장
            ObservableList<String> tasks = taskListView.getItems();

            // tasks 리스트에서 주어진 index 위치에 있는 항목을 수정
            tasks.set(index, updatedTask);
        }
    }

    /**
     * ListView에서 기존 작업을 삭제
     *
     * @param taskListView 삭제할 작업이 있는 ListView
     * @param index        삭제할 작업의 인덱스
     */
    public void deleteTask(ListView<String> taskListView, int index) {
        // 인덱스가 유효한지 확인한 후 작업을 삭제
        if (index >= 0 && index < taskListView.getItems().size()) {
            taskListView.getItems().remove(index);
        }
    }
}