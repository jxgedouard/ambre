package com.ambre.controller;

import com.ambre.service.AuthService;
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
import java.util.logging.Logger;

/**
 * Contrôleur de l'écran de création de compte.
 */
public class RegisterController {

    private static final Logger LOG = Logger.getLogger(RegisterController.class.getName());

    @FXML private Label titleLabel;
    @FXML private Label usernameLabel;
    @FXML private Label passwordLabel;
    @FXML private Label confirmLabel;
    @FXML private Label usernameError;
    @FXML private Label passwordError;
    @FXML private Label backLink;
    @FXML private Button registerBtn;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmField;

    private final I18n i18n = I18n.getInstance();
    private final AuthService authService = AuthService.getInstance();

    @FXML
    public void initialize() {
        applyI18n();
        i18n.addListener(() -> applyI18n());
    }

    private void applyI18n() {
        titleLabel.setText(i18n.get("register.title"));
        usernameLabel.setText(i18n.get("register.username"));
        passwordLabel.setText(i18n.get("register.password"));
        confirmLabel.setText(i18n.get("register.confirm"));
        registerBtn.setText(i18n.get("register.submit"));
        backLink.setText(i18n.get("register.back"));
    }

    @FXML
    private void onRegister() {
        // On efface les erreurs précédentes
        hideError(usernameError);
        hideError(passwordError);

        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirm = confirmField.getText();

        // AuthService retourne null si succès, ou une clé i18n d'erreur
        String errorKey = authService.register(username, password, confirm);

        if (errorKey != null) {
            // Afficher l'erreur sous le bon champ
            if (errorKey.equals("register.error.mismatch")) {
                showError(passwordError, i18n.get(errorKey));
            } else {
                showError(usernameError, i18n.get(errorKey));
            }
            return;
        }

        // Inscription réussie : retour à l'écran de login
        navigateTo("/com/ambre/fxml/login.fxml", 440, 520);
    }

    @FXML
    private void onBack() {
        navigateTo("/com/ambre/fxml/login.fxml", 440, 520);
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

            Stage stage = (Stage) registerBtn.getScene().getWindow();
            stage.setWidth(width);
            stage.setHeight(height);
            stage.setScene(newScene);
            ft.play();

        } catch (IOException e) {
            LOG.severe("Impossible de charger " + fxmlPath + " : " + e.getMessage());
        }
    }
}
