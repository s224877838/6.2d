package com.ontrack;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Task {
    private final String taskId;
    private final String studentId;
    private final String title;
    private final String content;
    private String status;
    private String feedback;
    private Double grade;
    private final List<ChatMessage> messages;
    private final Instant createdAt;

    public Task(String taskId, String studentId, String title, String content) {
        this.taskId = taskId;
        this.studentId = studentId;
        this.title = title;
        this.content = content;
        this.status = "submitted";
        this.feedback = null;
        this.grade = null;
        this.messages = new ArrayList<>();
        this.createdAt = Instant.now();
    }

    public String getTaskId() {
        return taskId;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public Double getGrade() {
        return grade;
    }

    public void setGrade(Double grade) {
        this.grade = grade;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
