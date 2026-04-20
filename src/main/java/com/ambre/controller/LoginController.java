package com.ambre.controller;

import com.ambre.model.Task;
import com.ambre.model.User;
import com.ambre.service.AuthService;
import com.ambre.service.NotificationService;
import com.ambre.service.TaskService;
import com.ambre.util.I18n;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class LoginController {

    private static final Logger LOG = Logger.getLogger(LoginController.class.getName());

    @FXML private Label titleLabel;
    @FXML private Label usernameLabel;
    @FXML private Label passwordLabel;
    @FXML private Label errorLabel;
    @FXML private Label registerLink;
    @FXML private Button loginBtn;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    private final I18n i18n = I18n.getInstance();
    private final AuthService authService = AuthService.getInstance();

    @FXML
    public void initialize() {
        applyI18n();
        i18n.addListener(() -> applyI18n());
    }

    private void applyI18n() {
        titleLabel.setText(i18n.get("login.title"));
        usernameLabel.setText(i18n.get("login.username"));
        passwordLabel.setText(i18n.get("login.password"));
        loginBtn.setText(i18n.get("login.submit"));
        registerLink.setText(i18n.get("login.register"));
    }

    @FXML
    private void onLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        boolean success = authService.login(username, password);

        if (!success) {
            showError(i18n.get("login.error.invalid"));
            return;
        }

        User user = authService.getCurrentUser();
        List<Task> tasks = TaskService.getInstance().getTasksForUser(user.getId());
        NotificationService.getInstance().notifyUpcoming(tasks);

        navigateTo("/com/ambre/fxml/main.fxml", 1100, 720);
    }

    @FXML
    private void onRegister() {
        navigateTo("/com/ambre/fxml/register.fxml", 440, 580);
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void navigateTo(String fxmlPath, int width, int height) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene newScene = new Scene(loader.load(), width, height);
            newScene.getStylesheets().add(
                getClass().getResource("/com/ambre/styles/ambre.css").toExternalForm()
            );

            FadeTransition ft = new FadeTransition(Duration.millis(200), newScene.getRoot());
            ft.setFromValue(0);
            ft.setToValue(1);

            Stage stage = (Stage) loginBtn.getScene().getWindow();
            stage.setWidth(width);
            stage.setHeight(height);
            stage.setScene(newScene);
            ft.play();

        } catch (IOException e) {
            LOG.severe("Impossible de charger " + fxmlPath + " : " + e.getMessage());
        }
    }
}
