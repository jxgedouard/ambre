package com.ambre.service;

import com.ambre.model.Task;
import com.ambre.model.Task.Status;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

/**
 * Envoie des notifications macOS pour les tâches dont la date limite approche.
 * Utilise la commande osascript (AppleScript) disponible sur macOS.
 */
public class NotificationService {

    private static final Logger LOG = Logger.getLogger(NotificationService.class.getName());

    private static final NotificationService INSTANCE = new NotificationService();

    private NotificationService() {}

    public static NotificationService getInstance() {
        return INSTANCE;
    }

    public void notifyUpcoming(List<Task> tasks) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoff = now.plusHours(24);

        for (Task t : tasks) {
            if (t.getStatus() == Status.DONE) continue;
            if (t.getDueDate() == null) continue;
            if (!t.getDueDate().isBefore(now) && t.getDueDate().isBefore(cutoff)) {
                sendNotification("Tâche à rendre bientôt", t.getTitle());
            }
        }
    }

    private void sendNotification(String subtitle, String body) {
        // Échapper les guillemets pour éviter une injection dans le script osascript
        String safeBody = body.replace("\\", "\\\\").replace("\"", "\\\"");

        String script = "display notification \"" + safeBody + "\" "
                      + "with title \"Ambre\" "
                      + "subtitle \"" + subtitle + "\"";

        try {
            Runtime.getRuntime().exec(new String[]{"osascript", "-e", script});
            LOG.info("Notification envoyée : " + body);
        } catch (IOException e) {
            LOG.warning("Impossible d'envoyer la notification : " + e.getMessage());
        }
    }
}
