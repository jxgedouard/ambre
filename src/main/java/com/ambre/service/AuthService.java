package com.ambre.service;

import com.ambre.model.User;
import com.ambre.util.JsonStorage;
import com.google.gson.reflect.TypeToken;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class AuthService {

    private static final Logger LOG = Logger.getLogger(AuthService.class.getName());

    private static final AuthService INSTANCE = new AuthService();

    private final JsonStorage<User> storage;
    private User currentUser;

    private AuthService() {
        storage = new JsonStorage<>("users.json", new TypeToken<List<User>>(){}.getType());
        currentUser = null;
    }

    public static AuthService getInstance() {
        return INSTANCE;
    }

    /** Retourne null si succès, ou une clé i18n d'erreur en cas d'échec. */
    public String register(String username, String password, String confirm) {
        if (username == null || username.isBlank() ||
            password == null || password.isBlank() ||
            confirm == null || confirm.isBlank()) {
            return "register.error.empty";
        }

        if (!password.equals(confirm)) {
            return "register.error.mismatch";
        }

        List<User> users = storage.readAll();
        for (User u : users) {
            if (u.getUsername().equalsIgnoreCase(username)) {
                return "register.error.taken";
            }
        }

        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        User newUser = new User(UUID.randomUUID().toString(), username, hash, LocalDateTime.now());
        users.add(newUser);
        storage.writeAll(users);

        LOG.info("Nouveau compte créé : " + username);
        return null; // null = succès
    }

    public boolean login(String username, String password) {
        List<User> users = storage.readAll();

        for (User u : users) {
            if (u.getUsername().equalsIgnoreCase(username)) {
                if (BCrypt.checkpw(password, u.getPasswordHash())) {
                    currentUser = u;
                    LOG.info("Connexion : " + username);
                    return true;
                }
            }
        }
        return false;
    }

    public void logout() {
        currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }
}
