package com.ambre.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Singleton de gestion des traductions FR/EN.
 * Les contrôleurs s'enregistrent via addListener() pour mettre à jour
 * leurs labels quand la langue change.
 */
public class I18n {

    private static final Logger LOG = Logger.getLogger(I18n.class.getName());

    private static final I18n INSTANCE = new I18n();

    private ResourceBundle bundle;
    private Locale currentLocale;
    private List<Runnable> listeners;

    private I18n() {
        currentLocale = Locale.FRENCH;
        listeners = new ArrayList<>();
        loadBundle();
    }

    public static I18n getInstance() {
        return INSTANCE;
    }

    private void loadBundle() {
        bundle = ResourceBundle.getBundle(
            "com.ambre.i18n.messages",
            currentLocale
        );
    }

    /** Retourne la traduction pour la clé donnée, ou la clé elle-même si absente. */
    public String get(String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            LOG.warning("Clé i18n manquante : " + key);
            return key;
        }
    }

    public void switchLocale() {
        currentLocale = currentLocale.equals(Locale.FRENCH) ? Locale.ENGLISH : Locale.FRENCH;
        loadBundle();
        for (Runnable listener : listeners) {
            listener.run();
        }
    }

    public Locale getCurrentLocale() {
        return currentLocale;
    }

    public void addListener(Runnable listener) {
        listeners.add(listener);
    }

    public void removeListener(Runnable listener) {
        listeners.remove(listener);
    }
}
