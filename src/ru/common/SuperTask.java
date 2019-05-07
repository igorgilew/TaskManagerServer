package ru.common;

import java.io.Serializable;

public abstract class SuperTask implements Serializable {
    private String title;
    private String description;
    private TaskStatus taskStatus;

    public SuperTask(String title, String description) {
        this.title = title;
        this.description = description;
        this.taskStatus = TaskStatus.InProgress;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(TaskStatus taskStatus) {
        this.taskStatus = taskStatus;
    }

    @Override
    public String toString() {
        return title;
    }
}
