package com.ambre.controller;

import com.ambre.model.Task;
import com.ambre.model.Task.Priority;
import com.ambre.model.Task.Status;
import com.ambre.util.I18n;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class TaskCell extends ListCell<Task> {

    private static final DateTimeFormatter FMT_FR = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.FRENCH);
    private static final DateTimeFormatter FMT_EN = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);

    private final Runnable onEdit;
    private final Runnable onDelete;
    private final Runnable onToggle;

    private Task currentTask;

    public TaskCell(Runnable onEdit, Runnable onDelete, Runnable onToggle) {
        this.onEdit = onEdit;
        this.onDelete = onDelete;
        this.onToggle = onToggle;
    }

    @Override
    protected void updateItem(Task task, boolean empty) {
        super.updateItem(task, empty);

        if (empty || task == null) {
            setGraphic(null);
            setStyle("-fx-background-color: transparent;");
            return;
        }

        this.currentTask = task;
        setGraphic(buildCard(task));
        setStyle("-fx-background-color: transparent; -fx-padding: 4 8 4 8;");
    }

    private VBox buildCard(Task task) {
        boolean isDone = task.getStatus() == Status.DONE;

        VBox card = new VBox(8);
        card.getStyleClass().add(isDone ? "task-card-done" : "task-card");
        card.setMaxWidth(Double.MAX_VALUE);

        HBox row1 = new HBox(10);
        row1.setAlignment(Pos.CENTER_LEFT);

        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(isDone);
        checkBox.setOnAction(e -> {
            currentTask = getItem();
            if (onToggle != null) onToggle.run();
        });

        Label titleLabel = new Label(task.getTitle());
        titleLabel.getStyleClass().add(isDone ? "task-title-done" : "task-title");
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(titleLabel, javafx.scene.layout.Priority.ALWAYS);

        Label badge = buildPriorityBadge(task.getPriority());

        Button editBtn = new Button("✎");
        editBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #7A6A55; -fx-cursor: hand;");
        editBtn.setOnAction(e -> {
            currentTask = getItem();
            if (onEdit != null) onEdit.run();
        });

        Button deleteBtn = new Button("✕");
        deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #C0392B; -fx-cursor: hand;");
        deleteBtn.setOnAction(e -> {
            currentTask = getItem();
            if (onDelete != null) onDelete.run();
        });

        row1.getChildren().addAll(checkBox, titleLabel, badge, editBtn, deleteBtn);

        String desc = task.getDescription() != null ? task.getDescription() : "";
        if (desc.length() > 60) {
            desc = desc.substring(0, 60) + "…";
        }
        Label descLabel = new Label(desc);
        descLabel.getStyleClass().add("task-desc");

        HBox row3 = new HBox(16);
        row3.setAlignment(Pos.CENTER_LEFT);

        Label dateLabel = buildDateLabel(task);

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Label progressLabel = new Label(task.getProgress() + "%");
        progressLabel.getStyleClass().add("label-muted");

        row3.getChildren().addAll(dateLabel, spacer, progressLabel);

        ProgressBar progressBar = new ProgressBar(task.getProgress() / 100.0);
        progressBar.setMaxWidth(Double.MAX_VALUE);

        card.getChildren().addAll(row1, descLabel, row3, progressBar);
        return card;
    }

    private Label buildPriorityBadge(Priority priority) {
        I18n i18n = I18n.getInstance();
        String text;
        String styleClass;

        if (priority == Priority.HIGH) {
            text = i18n.get("priority.high");
            styleClass = "badge-high";
        } else if (priority == Priority.MEDIUM) {
            text = i18n.get("priority.medium");
            styleClass = "badge-medium";
        } else {
            text = i18n.get("priority.low");
            styleClass = "badge-low";
        }

        Label badge = new Label(text);
        badge.getStyleClass().add(styleClass);
        return badge;
    }

    private Label buildDateLabel(Task task) {
        if (task.getDueDate() == null) {
            return new Label("");
        }

        I18n i18n = I18n.getInstance();
        DateTimeFormatter fmt = i18n.getCurrentLocale().equals(Locale.FRENCH) ? FMT_FR : FMT_EN;
        String dateText = task.getDueDate().format(fmt);

        Label label = new Label(dateText);

        boolean overdue = task.getDueDate().isBefore(LocalDateTime.now())
                       && task.getStatus() != Status.DONE;
        label.getStyleClass().add(overdue ? "task-date-overdue" : "task-date");

        return label;
    }

    public Task getCurrentTask() {
        return currentTask;
    }
}
