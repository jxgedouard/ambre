package com.ambre.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonPrimitive;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Lecture/écriture générique d'une liste d'objets dans un fichier JSON.
 * Les fichiers sont stockés dans ~/.ambre/
 */
public class JsonStorage<T> {

    private static final Logger LOG = Logger.getLogger(JsonStorage.class.getName());

    private static final String BASE_DIR = System.getProperty("user.home") + "/.ambre/";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // Gson configuré pour sérialiser/désérialiser LocalDateTime en String ISO
    private static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(LocalDateTime.class,
            (JsonSerializer<LocalDateTime>) (src, type, ctx) ->
                new JsonPrimitive(src.format(DATE_FORMAT)))
        .registerTypeAdapter(LocalDateTime.class,
            (JsonDeserializer<LocalDateTime>) (json, type, ctx) ->
                LocalDateTime.parse(json.getAsString(), DATE_FORMAT))
        .setPrettyPrinting()
        .create();

    private final File file;
    private final Type listType;

    public JsonStorage(String filename, Type listType) {
        this.file = new File(BASE_DIR + filename);
        this.listType = listType;
        file.getParentFile().mkdirs();
    }

    /** Retourne la liste depuis le fichier JSON, ou une liste vide si le fichier n'existe pas. */
    public List<T> readAll() {
        if (!file.exists()) {
            return new ArrayList<>();
        }
        try (FileReader reader = new FileReader(file)) {
            List<T> result = GSON.fromJson(reader, listType);
            if (result == null) {
                return new ArrayList<>();
            }
            return result;
        } catch (IOException e) {
            LOG.severe("Erreur lecture " + file.getPath() + " : " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /** Remplace le contenu du fichier JSON par la liste donnée. */
    public void writeAll(List<T> items) {
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(items, listType, writer);
        } catch (IOException e) {
            LOG.severe("Erreur écriture " + file.getPath() + " : " + e.getMessage());
        }
    }
}
