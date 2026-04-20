package com.ambre.controller;

import com.ambre.model.Task;
import com.ambre.model.Task.Status;
import com.ambre.model.User;
import com.ambre.service.AuthService;
import com.ambre.service.TaskService;
import com.ambre.util.I18n;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class MainController {

    private static final Logger LOG = Logger.getLogger(MainController.class.getName());

    @FXML private Label appTitleLabel;
    @FXML private Label userLabel;
    @FXML private Button langBtn;
    @FXML private Button logoutBtn;
    @FXML private Button filterAllBtn;
    @FXML private Button filterInProgressBtn;
    @FXML private Button filterDoneBtn;
    @FXML private Button addBtn;
    @FXML private ListView<Task> taskListView;

    private final I18n i18n = I18n.getInstance();
    private final AuthService authService = AuthService.getInstance();
    private final TaskService taskService = TaskService.getInstance();

    // Filtre actif : "all", "inprogress", ou "done"
    private String activeFilter = "all";

    @FXML
    public void initialize() {
        applyI18n();
        i18n.addListener(() -> applyI18n());

        taskListView.setCellFactory(lv -> {
            TaskCell[] ref = new TaskCell[1];
            ref[0] = new TaskCell(
                () -> onEditTask(ref[0]),
                () -> onDeleteTask(ref[0]),
                () -> onToggleDone(ref[0])
            );
            return ref[0];
        });

        User user = authService.getCurrentUser();
        if (user != null) {
            userLabel.setText(user.getUsername());
        }

        refreshList();
        updateFilterStyles();
    }

    private void applyI18n() {
        appTitleLabel.setText(i18n.get("app.title"));
        langBtn.setText(i18n.get("nav.lang"));
        logoutBtn.setText(i18n.get("main.logout"));
        filterAllBtn.setText(i18n.get("main.filter.all"));
        filterInProgressBtn.setText(i18n.get("main.filter.inprogress"));
        filterDoneBtn.setText(i18n.get("main.filter.done"));
        addBtn.setText(i18n.get("main.add"));
        // Rafraîchir la liste pour mettre à jour les badges de priorité
        taskListView.refresh();
    }

    private void refreshList() {
        User user = authService.getCurrentUser();
        if (user == null) return;

        List<Task> allTasks = taskService.getTasksForUser(user.getId());
        List<Task> filteredTasks = new ArrayList<>();

        for (Task t : allTasks) {
            if (activeFilter.equals("all")) {
                filteredTasks.add(t);
            } else if (activeFilter.equals("inprogress") && t.getStatus() != Status.DONE) {
                filteredTasks.add(t);
            } else if (activeFilter.equals("done") && t.getStatus() == Status.DONE) {
                filteredTasks.add(t);
            }
        }

        ObservableList<Task> items = FXCollections.observableArrayList(filteredTasks);
        taskListView.setItems(items);
    }

    @FXML private void onFilterAll()        { activeFilter = "all";        refreshList(); updateFilterStyles(); }
    @FXML private void onFilterInProgress() { activeFilter = "inprogress"; refreshList(); updateFilterStyles(); }
    @FXML private void onFilterDone()       { activeFilter = "done";       refreshList(); updateFilterStyles(); }

    private void updateFilterStyles() {
        filterAllBtn.getStyleClass().removeAll("filter-btn", "filter-btn-active");
        filterInProgressBtn.getStyleClass().removeAll("filter-btn", "filter-btn-active");
        filterDoneBtn.getStyleClass().removeAll("filter-btn", "filter-btn-active");

        filterAllBtn.getStyleClass().add(activeFilter.equals("all") ? "filter-btn-active" : "filter-btn");
        filterInProgressBtn.getStyleClass().add(activeFilter.equals("inprogress") ? "filter-btn-active" : "filter-btn");
        filterDoneBtn.getStyleClass().add(activeFilter.equals("done") ? "filter-btn-active" : "filter-btn");
    }

    @FXML
    private void onAddTask() {
        User user = authService.getCurrentUser();
        if (user == null) return;
        openTaskForm(null, user.getId());
    }

    private void onEditTask(TaskCell cell) {
        Task task = cell.getCurrentTask();
        if (task != null) {
            openTaskForm(task, task.getUserId());
        }
    }

    private void onDeleteTask(TaskCell cell) {
        Task task = cell.getCurrentTask();
        if (task == null) return;

        // Boîte de dialogue de confirmation stylisée
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(i18n.get("task.delete.confirm"));
        alert.setHeaderText(i18n.get("task.delete.confirm"));
        alert.setContentText(i18n.get("task.delete.content"));
        alert.getDialogPane().getStylesheets().add(
            getClass().getResource("/com/ambre/styles/ambre.css").toExternalForm()
        );

        ButtonType yesBtn = new ButtonType(i18n.get("task.delete.yes"), ButtonBar.ButtonData.YES);
        ButtonType noBtn  = new ButtonType(i18n.get("task.delete.no"),  ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(yesBtn, noBtn);

        alert.showAndWait().ifPresent(result -> {
            if (result == yesBtn) {
                taskService.deleteTask(task.getId());
                refreshList();
            }
        });
    }

    private void onToggleDone(TaskCell cell) {
        Task task = cell.getCurrentTask();
        if (task == null) return;

        if (task.getStatus() == Status.DONE) {
            // Décocher : remettre en IN_PROGRESS
            task.setStatus(Status.IN_PROGRESS);
            taskService.updateTask(task);
        } else {
            // Cocher : marquer comme terminée
            taskService.markDone(task.getId());
        }
        refreshList();
    }

    // task == null → création, non-null → modification
    private void openTaskForm(Task task, String userId) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/ambre/fxml/task_form.fxml")
            );
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(
                getClass().getResource("/com/ambre/styles/ambre.css").toExternalForm()
            );

            TaskFormController ctrl = loader.getController();
            if (task == null) {
                ctrl.initForAdd(userId, () -> refreshList());
            } else {
                ctrl.initForEdit(task, () -> refreshList());
            }

            Stage modal = new Stage();
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.initOwner(taskListView.getScene().getWindow());
            modal.setResizable(false);
            modal.setScene(scene);

            FadeTransition ft = new FadeTransition(Duration.millis(200), scene.getRoot());
            ft.setFromValue(0);
            ft.setToValue(1);
            modal.show();
            ft.play();

        } catch (IOException e) {
            LOG.severe("Impossible d'ouvrir le formulaire de tâche : " + e.getMessage());
        }
    }

    @FXML
    private void onSwitchLang() {
        i18n.switchLocale();
    }

    @FXML
    private void onLogout() {
        authService.logout();
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/ambre/fxml/login.fxml")
            );
            Scene newScene = new Scene(loader.load(), 440, 520);
            newScene.getStylesheets().add(
                getClass().getResource("/com/ambre/styles/ambre.css").toExternalForm()
            );

            FadeTransition ft = new FadeTransition(Duration.millis(200), newScene.getRoot());
            ft.setFromValue(0);
            ft.setToValue(1);

            Stage stage = (Stage) logoutBtn.getScene().getWindow();
            stage.setWidth(440);
            stage.setHeight(520);
            stage.setScene(newScene);
            ft.play();

        } catch (IOException e) {
            LOG.severe("Erreur lors de la déconnexion : " + e.getMessage());
        }
    }
}
