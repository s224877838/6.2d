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
        return service.submitTask("s12345", "Week 1 Quiz", "My answer content");
    }

    // R = Right result
    @Test
    void submitAndViewTaskReturnsExpectedData() {
        String taskId = seedOneTask();
        Task task = service.viewInboxTask("s12345", taskId);
        assertEquals(taskId, task.getTaskId());
        assertEquals("Week 1 Quiz", task.getTitle());
        assertEquals("submitted", task.getStatus());
    }

    // B = Boundary
    @Test
    void titleBoundaryAllowsExactMaxLength() {
        String title = "A".repeat(OnTrackService.MAX_TITLE_LENGTH);
        String taskId = service.submitTask("s1000", title, "boundary test");
        Task task = service.viewInboxTask("s1000", taskId);
        assertEquals(title, task.getTitle());
    }

    @Test
    void gradeBoundaryRejectsOutOfRange() {
        String taskId = seedOneTask();
        assertThrows(IllegalArgumentException.class,
                () -> service.updateFeedback(taskId, "t001", "completed", "Too low", -0.1));
        assertThrows(IllegalArgumentException.class,
                () -> service.updateFeedback(taskId, "t001", "completed", "Too high", 100.1));
    }

    // I = Inverse
    @Test
    void viewTaskDeniesWrongStudentInverseCase() {
        String taskId = seedOneTask();
        assertThrows(SecurityException.class, () -> service.viewInboxTask("s99999", taskId));
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
        Task task = service.viewInboxTask("s12345", taskId);
        assertEquals("in_review", task.getStatus());
        assertEquals("Please improve intro", task.getFeedback());
        assertEquals(65.5, task.getGrade());
    }

    // E = Error handling
    @Test
    void errorNonExistentTaskRaisesException() {
        assertThrows(IllegalArgumentException.class, () -> service.viewInboxTask("s12345", "TASK-4040"));
    }

    // P = Performance
    @Test
    void inboxResponseTimeUnderThresholdForSmallDataset() {
        for (int i = 0; i < 300; i++) {
            service.submitTask("s12345", "Task " + i, "bulk");
        }
        double elapsed = service.metricsInboxResponseTime("s12345");
        assertTrue(elapsed < 0.1);
        assertFalse(Double.isNaN(elapsed));
    }
}
