package com.example.todo_timer;

public class Task {
    private String projectName; // 프로젝트 이름
    private String name; // 작업 이름


    /**
     * Task 클래스의 생성자
     *
     * @param name 작업 이름
     * @param projectName 작업이 속한 프로젝트 이름
     */
    public Task(String name, String projectName) {
        this.name = name;
        this.projectName = projectName;
    }

    /**
     * Task 객체를 문자열로 표현하는 메서드
     *
     * @return Task 객체의 문자열 표현
     */
    @Override
    public String toString() {
        return "[" + projectName + "] - " + name;
    }


    /**
     * 작업의 이름을 반환하는 메서드
     *
     * @return 작업의 이름
     */
    public String getName() {
        return name;
    }

    /**
     * 작업이 속한 프로젝트의 이름을 반환하는 메서드
     *
     * @return 프로젝트의 이름
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * 프로젝트 이름을 설정하는 메서드
     *
     * @param projectName 새로운 프로젝트 이름
     */
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
}