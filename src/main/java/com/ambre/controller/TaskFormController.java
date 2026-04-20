package com.ambre.controller;

import com.ambre.model.Task;
import com.ambre.model.Task.Priority;
import com.ambre.service.TaskService;
import com.ambre.util.I18n;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class TaskFormController {

    @FXML private Label formTitleLabel;
    @FXML private Label titleLabel;
    @FXML private Label descLabel;
    @FXML private Label dueDateLabel;
    @FXML private Label priorityLabel;
    @FXML private Label progressLabel;
    @FXML private Label progressValueLabel;
    @FXML private Label titleError;
    @FXML private Label dateError;
    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;
    @FXML private TextField titleField;
    @FXML private TextArea descField;
    @FXML private DatePicker dueDatePicker;
    @FXML private ChoiceBox<String> priorityChoice;
    @FXML private Slider progressSlider;

    private final I18n i18n = I18n.getInstance();
    private final TaskService taskService = TaskService.getInstance();

    private Task editingTask; // null si création
    private String userId;
    private Runnable onSaveCallback;

    @FXML
    public void initialize() {
        applyI18n();
        i18n.addListener(() -> applyI18n());

        // Mise à jour du label "60%" en temps réel quand le slider bouge
        progressSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            progressValueLabel.setText((int) newValue.doubleValue() + "%");
        });
        progressValueLabel.setText("0%");
    }

    private void applyI18n() {
        titleLabel.setText(i18n.get("task.title"));
        descLabel.setText(i18n.get("task.description"));
        dueDateLabel.setText(i18n.get("task.duedate"));
        priorityLabel.setText(i18n.get("task.priority"));
        progressLabel.setText(i18n.get("task.progress"));
        saveBtn.setText(i18n.get("task.save"));
        cancelBtn.setText(i18n.get("task.cancel"));

        // Remplir la liste déroulante des priorités
        priorityChoice.getItems().setAll(
            i18n.get("priority.low"),
            i18n.get("priority.medium"),
            i18n.get("priority.high")
        );
        if (priorityChoice.getValue() == null) {
            priorityChoice.setValue(i18n.get("priority.medium"));
        }
    }

    public void initForAdd(String userId, Runnable callback) {
        this.userId = userId;
        this.onSaveCallback = callback;
        this.editingTask = null;
        formTitleLabel.setText(i18n.get("task.add.title"));
        priorityChoice.setValue(i18n.get("priority.medium"));
        progressSlider.setValue(0);
    }

    public void initForEdit(Task task, Runnable callback) {
        this.editingTask = task;
        this.userId = task.getUserId();
        this.onSaveCallback = callback;
        formTitleLabel.setText(i18n.get("task.edit.title"));

        titleField.setText(task.getTitle());
        descField.setText(task.getDescription() != null ? task.getDescription() : "");
        if (task.getDueDate() != null) {
            dueDatePicker.setValue(task.getDueDate().toLocalDate());
        }
        priorityChoice.setValue(priorityToLabel(task.getPriority()));
        progressSlider.setValue(task.getProgress());
    }

    private String priorityToLabel(Priority priority) {
        if (priority == Priority.HIGH)   return i18n.get("priority.high");
        if (priority == Priority.LOW)    return i18n.get("priority.low");
        return i18n.get("priority.medium");
    }

    private Priority labelToPriority() {
        String selected = priorityChoice.getValue();
        if (selected.equals(i18n.get("priority.high"))) return Priority.HIGH;
        if (selected.equals(i18n.get("priority.low")))  return Priority.LOW;
        return Priority.MEDIUM;
    }

    @FXML
    private void onSave() {
        // Effacer les erreurs précédentes
        hideError(titleError);
        hideError(dateError);

        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            showError(titleError, i18n.get("error.title.required"));
            return;
        }

        if (dueDatePicker.getValue() == null) {
            showError(dateError, i18n.get("error.duedate.required"));
            return;
        }

        LocalDateTime dueDate = dueDatePicker.getValue().atTime(LocalTime.of(23, 59));
        int progress = (int) progressSlider.getValue();
        Priority priority = labelToPriority();
        String desc = descField.getText().trim();

        if (editingTask != null) {
            editingTask.setTitle(title);
            editingTask.setDescription(desc);
            editingTask.setDueDate(dueDate);
            editingTask.setPriority(priority);
            editingTask.setProgress(progress);
            taskService.updateTask(editingTask);
        } else {
            Task newTask = taskService.buildNew(userId, title, desc, dueDate, priority, progress);
            taskService.addTask(newTask);
        }

        if (onSaveCallback != null) {
            onSaveCallback.run();
        }

        close();
    }

    @FXML
    private void onCancel() {
        close();
    }

    private void close() {
        Stage stage = (Stage) saveBtn.getScene().getWindow();
        stage.close();
    }

    private void showError(Label label, String message) {
        label.setText(message);
        label.setVisible(true);
        label.setManaged(true);
    }

    private void hideError(Label label) {
        label.setVisible(false);
        label.setManaged(false);
    }
}
