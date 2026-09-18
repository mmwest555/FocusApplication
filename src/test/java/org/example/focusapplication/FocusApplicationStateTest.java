package org.example.focusapplication;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class FocusApplicationStateTest {
    @Test
    void loginStatusMessageCanBeStoredForNextLoginScene() throws Exception {
        FocusApplication application = new FocusApplication();
        Field field = FocusApplication.class.getDeclaredField("loginStatusMessage");
        field.setAccessible(true);
        field.set(application, "Account created successfully. Please sign in.");

        assertEquals("Account created successfully. Please sign in.", field.get(application));
    }

    @Test
    void consumeLoginStatusMessageClearsPendingValue() throws Exception {
        FocusApplication application = new FocusApplication();
        Field field = FocusApplication.class.getDeclaredField("loginStatusMessage");
        field.setAccessible(true);
        field.set(application, "Account created successfully. Please sign in.");

        assertEquals("Account created successfully. Please sign in.", application.consumeLoginStatusMessage());
        assertNull(field.get(application));
    }

    @Test
    void saveTaskCreatesAndSortsTasksByTimeFrame() {
        FocusApplication application = new FocusApplication();

        application.saveTask(null, "Later task", LocalTime.of(15, 0), LocalTime.of(16, 0), "Work");
        application.saveTask(null, "Sooner task", LocalTime.of(13, 0), LocalTime.of(14, 0), "Personal");

        assertEquals(2, application.getTasks().size());
        assertIterableEquals(
                java.util.List.of("Sooner task", "Later task"),
                application.getTasks().stream().map(FocusApplication.FocusTask::getTitle).toList()
        );
    }

    @Test
    void saveTaskUpdatesExistingTaskWithoutDuplicatingIt() {
        FocusApplication application = new FocusApplication();
        application.saveTask(null, "Original task", LocalTime.of(14, 0), LocalTime.of(15, 0), "Study");

        FocusApplication.FocusTask task = application.getTasks().getFirst();
        application.saveTask(task, "Updated task", LocalTime.of(9, 0), LocalTime.of(11, 0), "Health");

        assertEquals(1, application.getTasks().size());
        FocusApplication.FocusTask updatedTask = application.getTasks().getFirst();
        assertEquals("Updated task", updatedTask.getTitle());
        assertEquals(LocalTime.of(9, 0), updatedTask.getStartTime());
        assertEquals(LocalTime.of(11, 0), updatedTask.getEndTime());
        assertEquals("Health", updatedTask.getTaskType());
    }

    @Test
    void deleteTaskRemovesTaskFromCollection() {
        FocusApplication application = new FocusApplication();
        application.saveTask(null, "Disposable task", LocalTime.of(10, 0), LocalTime.of(10, 30), "Errand");

        FocusApplication.FocusTask task = application.getTasks().getFirst();
        application.deleteTask(task);

        assertEquals(0, application.getTasks().size());
    }
}
