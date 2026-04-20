package com.ambre.service;

import com.ambre.model.Task;
import com.ambre.model.Task.Priority;
import com.ambre.model.Task.Status;
import com.ambre.util.JsonStorage;
import com.google.gson.reflect.TypeToken;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class TaskService {

    private static final Logger LOG = Logger.getLogger(TaskService.class.getName());

    private static final TaskService INSTANCE = new TaskService();

    private final JsonStorage<Task> storage;

    private TaskService() {
        storage = new JsonStorage<>("tasks.json", new TypeToken<List<Task>>(){}.getType());
    }

    public static TaskService getInstance() {
        return INSTANCE;
    }

    /** Retourne les tâches de l'utilisateur triées : HIGH > MEDIUM > LOW, puis dueDate croissante. */
    public List<Task> getTasksForUser(String userId) {
        List<Task> all = storage.readAll();
        List<Task> userTasks = new ArrayList<>();

        for (Task t : all) {
            if (userId.equals(t.getUserId())) {
                userTasks.add(t);
            }
        }

        for (int i = 0; i < userTasks.size() - 1; i++) {
            for (int j = 0; j < userTasks.size() - 1 - i; j++) {
                if (compareTasks(userTasks.get(j), userTasks.get(j + 1)) > 0) {
                    Task temp = userTasks.get(j);
                    userTasks.set(j, userTasks.get(j + 1));
                    userTasks.set(j + 1, temp);
                }
            }
        }

        return userTasks;
    }

    private int compareTasks(Task t1, Task t2) {
        int p1 = priorityValue(t1.getPriority());
        int p2 = priorityValue(t2.getPriority());

        if (p1 != p2) {
            return p1 - p2; // priorité plus haute d'abord
        }

        // À même priorité, trier par date croissante
        if (t1.getDueDate() != null && t2.getDueDate() != null) {
            return t1.getDueDate().compareTo(t2.getDueDate());
        }
        return 0;
    }

    private int priorityValue(Priority p) {
        if (p == Priority.HIGH)   return 0;
        if (p == Priority.MEDIUM) return 1;
        return 2; // LOW
    }

    public void addTask(Task task) {
        List<Task> tasks = storage.readAll();
        tasks.add(task);
        storage.writeAll(tasks);
        LOG.info("Tâche ajoutée : " + task.getTitle());
    }

    public void updateTask(Task updated) {
        List<Task> tasks = storage.readAll();
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getId().equals(updated.getId())) {
                tasks.set(i, updated);
                break;
            }
        }
        storage.writeAll(tasks);
    }

    public void deleteTask(String taskId) {
        List<Task> tasks = storage.readAll();
        List<Task> updated = new ArrayList<>();
        for (Task t : tasks) {
            if (!t.getId().equals(taskId)) {
                updated.add(t);
            }
        }
        storage.writeAll(updated);
        LOG.info("Tâche supprimée : " + taskId);
    }

    public void markDone(String taskId) {
        List<Task> tasks = storage.readAll();
        for (Task t : tasks) {
            if (t.getId().equals(taskId)) {
                t.setStatus(Status.DONE);
                t.setProgress(100);
                break;
            }
        }
        storage.writeAll(tasks);
    }

    public Task buildNew(String userId, String title, String description,
                         LocalDateTime dueDate, Priority priority, int progress) {
        return new Task(
            UUID.randomUUID().toString(),
            userId,
            title,
            description,
            LocalDateTime.now(),
            dueDate,
            priority,
            Status.TODO,
            progress
        );
    }
}
