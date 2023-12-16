package com.example.todo_timer;

public class Task {
    private String name;
    private String projectName;

    public Task(String name, String projectName) {
        this.name = name;
        this.projectName = projectName;
    }

    // toString 메서드를 오버라이드하여 ChoiceBox에서 표시될 문자열 형식을 정의
    @Override
    public String toString() {
        return "[" + projectName + "] - " + name;
    }

    public String getName() {
        return name;
    }

    public String getProjectName() {
        return projectName;
    }
}