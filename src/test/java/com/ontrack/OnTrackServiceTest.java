package com.ontrack;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OnTrackServiceTest {
    private OnTrackService service;

    @BeforeEach
    void setup() {
        service = new OnTrackService();
    }

    private String seedOneTask() {
        return service.submitTask("s224877838", "Week 1 Quiz", "My answer content");
    }

    // R = Right result
    @Test
    void submitAndViewTaskReturnsExpectedData() {
        String taskId = seedOneTask();
        Task task = service.viewInboxTask("s224877838", taskId);
        assertEquals(taskId, task.getTaskId());
        assertEquals("Week 1 Quiz", task.getTitle());
        assertEquals("submitted", task.getStatus());
    }

    // B = Boundary
    @Test
    void titleBoundaryAllowsExactMaxLength() {
        String title = "A".repeat(OnTrackService.MAX_TITLE_LENGTH);
        String taskId = service.submitTask("s224877837", title, "boundary test");
        Task task = service.viewInboxTask("s224877837", taskId);
        assertEquals(title, task.getTitle());
    }

    @Test
    void studentIdBoundaryRejectsBelowMinimum() {
        assertThrows(IllegalArgumentException.class,
                () -> service.submitTask("s22487783", "Week 1 Quiz", "My answer content"));
    }

    @Test
    void studentIdBoundaryAllowsExactMinimum() {
        String studentId = "s224877837";
        String taskId = service.submitTask(studentId, "Week 1 Quiz", "My answer content");
        Task task = service.viewInboxTask(studentId, taskId);
        assertEquals(studentId, task.getStudentId());
    }

    @Test
    void gradeBoundaryRejectsOutOfRange() {
        String taskId = seedOneTask();
        assertThrows(IllegalArgumentException.class,
                () -> service.updateFeedback(taskId, "t001", "completed", "Too low", -0.1));
        assertThrows(IllegalArgumentException.class,
                () -> service.updateFeedback(taskId, "t001", "completed", "Too high", 100.1));
    }

    @Test
    void messageBoundaryAllowsExactMaxLength() {
        String taskId = seedOneTask();
        String message = "M".repeat(OnTrackService.MAX_MESSAGE_LENGTH);
        service.postMessage(taskId, "student", "s224877838", message);
        Task task = service.viewInboxTask("s224877838", taskId);
        assertEquals(1, task.getMessages().size());
        assertEquals(message, task.getMessages().get(0).getMessage());
    }

    @Test
    void messageBoundaryRejectsAboveMaxLength() {
        String taskId = seedOneTask();
        String message = "M".repeat(OnTrackService.MAX_MESSAGE_LENGTH + 1);
        assertThrows(IllegalArgumentException.class,
                () -> service.postMessage(taskId, "student", "s224877838", message));
    }

    // I = Inverse
    @Test
    void viewTaskDeniesWrongStudentInverseCase() {
        String taskId = seedOneTask();
        assertThrows(SecurityException.class, () -> service.viewInboxTask("s224877839", taskId));
    }

    @Test
    void invalidSenderRoleInverseCase() {
        String taskId = seedOneTask();
        assertThrows(IllegalArgumentException.class, () -> service.postMessage(taskId, "admin", "a01", "Invalid role"));
    }

    // C = Cross-check
    @Test
    void crossCheckFeedbackFlowUpdatesViewModel() {
        String taskId = seedOneTask();
        service.updateFeedback(taskId, "t001", "in_review", "Please improve intro", 65.5);
        Task task = service.viewInboxTask("s224877838", taskId);
        assertEquals("in_review", task.getStatus());
        assertEquals("Please improve intro", task.getFeedback());
        assertEquals(65.5, task.getGrade());
    }

    // E = Error handling
    @Test
    void errorNonExistentTaskRaisesException() {
        assertThrows(IllegalArgumentException.class, () -> service.viewInboxTask("s224877838", "TASK-4040"));
    }

    @Test
    void emptyInboxReturnsEmptyList() {
        assertTrue(service.taskInbox("s224877839").isEmpty());
    }

    // P = Performance
    @Test
    void inboxResponseTimeUnderThresholdForSmallDataset() {
        for (int i = 0; i < 300; i++) {
            service.submitTask("s224877838", "Task " + i, "bulk");
        }
        double elapsed = service.metricsInboxResponseTime("s224877838");
        assertTrue(elapsed < 0.1);
        assertFalse(Double.isNaN(elapsed));
    }
}
