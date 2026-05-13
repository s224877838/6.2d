package com.ontrack;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OnTrackService {
    public static final int STUDENT_ID_DIGITS = 9;
    public static final int MAX_TITLE_LENGTH = 100;
    public static final int MAX_MESSAGE_LENGTH = 500;
    private static final Set<String> VALID_STATUSES = new HashSet<>(Set.of("submitted", "in_review", "completed"));

    private final Map<String, Task> tasks = new HashMap<>();
    private int taskCounter = 0;

    public String submitTask(String studentId, String title, String content) {
        validateStudentId(studentId);
        validateTitle(title);

        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Task content cannot be empty.");
        }

        taskCounter++;
        String taskId = String.format("TASK-%04d", taskCounter);
        Task task = new Task(taskId, studentId.trim(), title.trim(), content.trim());
        tasks.put(taskId, task);
        return taskId;
    }

    public List<Task> taskInbox(String studentId) {
        validateStudentId(studentId);
        List<Task> studentTasks = new ArrayList<>();
        for (Task task : tasks.values()) {
            if (task.getStudentId().equals(studentId.trim())) {
                studentTasks.add(task);
            }
        }
        studentTasks.sort(Comparator.comparing(Task::getCreatedAt).reversed());
        return studentTasks;
    }

    public Task viewInboxTask(String studentId, String taskId) {
        validateStudentId(studentId);
        Task task = requireTask(taskId);
        if (!task.getStudentId().equals(studentId.trim())) {
            throw new SecurityException("Student cannot view another student's task.");
        }
        return task;
    }

    public void postMessage(String taskId, String senderRole, String senderId, String message) {
        Task task = requireTask(taskId);
        if (!"student".equals(senderRole) && !"tutor".equals(senderRole)) {
            throw new IllegalArgumentException("Sender role must be 'student' or 'tutor'.");
        }
        if (senderId == null || senderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Sender ID cannot be empty.");
        }
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Message cannot be empty.");
        }
        if (message.trim().length() > MAX_MESSAGE_LENGTH) {
            throw new IllegalArgumentException("Message exceeds max length.");
        }
        task.getMessages().add(new ChatMessage(senderRole, senderId.trim(), message.trim()));
    }

    public void updateFeedback(String taskId, String tutorId, String status, String feedback, double grade) {
        Task task = requireTask(taskId);
        if (tutorId == null || tutorId.trim().isEmpty()) {
            throw new IllegalArgumentException("Tutor ID cannot be empty.");
        }
        if (!VALID_STATUSES.contains(status)) {
            throw new IllegalArgumentException("Invalid status.");
        }
        if (feedback == null || feedback.trim().isEmpty()) {
            throw new IllegalArgumentException("Feedback cannot be empty.");
        }
        if (grade < 0 || grade > 100) {
            throw new IllegalArgumentException("Grade must be between 0 and 100.");
        }

        task.setStatus(status);
        task.setFeedback(feedback.trim());
        task.setGrade(roundTo2(grade));
    }

    public double metricsInboxResponseTime(String studentId) {
        long startNanos = System.nanoTime();
        taskInbox(studentId);
        long elapsedNanos = System.nanoTime() - startNanos;
        return elapsedNanos / 1_000_000_000.0;
    }

    private Task requireTask(String taskId) {
        Task task = tasks.get(taskId);
        if (task == null) {
            throw new IllegalArgumentException("Task does not exist.");
        }
        return task;
    }

    private static void validateStudentId(String studentId) {
        if (studentId == null || studentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Student ID cannot be empty.");
        }
        String normalizedId = studentId.trim();
        if (!normalizedId.matches("^s\\d{" + STUDENT_ID_DIGITS + "}$")) {
            throw new IllegalArgumentException("Student ID must match format s#########.");
        }
    }

    private static void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty.");
        }
        if (title.trim().length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException("Title length must be <= " + MAX_TITLE_LENGTH);
        }
    }

    private static double roundTo2(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
