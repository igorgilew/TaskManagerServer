package ru.common;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class Task extends SuperTask {
    private LocalDate deadline;
    private String tags;
    private List<SubTask> subTasks;

    public Task(String title, String description, LocalDate deadline, String tags) {
        super(title, description);
        this.deadline = deadline;
        this.tags = tags;
        this.subTasks = new ArrayList<>();
    }

    public List<SubTask> getSubTasks() {
        return subTasks;
    }

    public List<String> getSplittedTags() {
        return Arrays.stream(tags.split(" ")).collect(Collectors.toList());
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public String getTags() {
        return tags;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

}