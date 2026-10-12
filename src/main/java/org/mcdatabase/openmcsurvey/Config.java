package org.mcdatabase.openmcsurvey;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class Config {

    private static final Gson GSON = new Gson();

    public static final String OPT_IN = "optIn";
    public static final String FIRST_SENT = "firstSent";

    private final Path configFile;

    public Config(String fileName) {
        this(FabricLoader.getInstance().getConfigDir().resolve(fileName));
    }

    public Config(Path configFile) {
        this.configFile = configFile;
    }

    public Path configFile() {
        return configFile;
    }

    public synchronized void ensureExists() throws IOException {
        if (Files.exists(configFile)) {
            return;
        }
        write(defaults());
    }

    public synchronized boolean getBoolean(String key, boolean defaultValue) {
        try {
            JsonObject root = readOrRepair();
            if (root == null || !root.has(key)) {
                return defaultValue;
            }
            JsonElement element = root.get(key);
            if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isBoolean()) {
                return defaultValue;
            }
            return element.getAsBoolean();
        } catch (Exception e) {
            OpenMCSurvey.LOGGER.warn("Failed to read config key '{}', assuming default", key, e);
            return defaultValue;
        }
    }

    public synchronized void setBoolean(String key, boolean value) {
        try {
            JsonObject root = readOrRepair();
            if (root == null) {
                root = defaults();
            }
            root.addProperty(key, value);
            write(root);
        } catch (Exception e) {
            OpenMCSurvey.LOGGER.warn("Failed to write config key '{}'", key, e);
        }
    }

    public boolean isOptedIn() {
        return getBoolean(OPT_IN, false);
    }

    public void setOptedIn(boolean value) {
        setBoolean(OPT_IN, value);
    }

    public boolean hasSentFirst() {
        return getBoolean(FIRST_SENT, false);
    }

    public void markFirstSent() {
        setBoolean(FIRST_SENT, true);
    }

    private JsonObject read() throws IOException {
        try (Reader reader = Files.newBufferedReader(configFile, StandardCharsets.UTF_8)) {
            JsonElement parsed;
            try {
                parsed = com.google.gson.JsonParser.parseReader(reader);
            } catch (JsonParseException e) {
                throw new IOException("Invalid JSON in config file", e);
            }
            if (parsed == null || parsed.isJsonNull()) {
                throw new IOException("Empty config file");
            }
            if (!parsed.isJsonObject()) {
                throw new IOException("Config root is not a JSON object");
            }
            return parsed.getAsJsonObject();
        }
    }

    private JsonObject readOrRepair() {
        if (!Files.exists(configFile)) {
            JsonObject fresh = defaults();
            try {
                write(fresh);
            } catch (IOException e) {
                OpenMCSurvey.LOGGER.warn("Failed to create config file", e);
            }
            return fresh;
        }
        try {
            return read();
        } catch (Exception e) {
            OpenMCSurvey.LOGGER.warn("Config file corrupt, resetting to defaults (opted out)", e);
            JsonObject fresh = defaults();
            try {
                write(fresh);
            } catch (IOException writeFailure) {
                OpenMCSurvey.LOGGER.warn("Failed to repair corrupt config file", writeFailure);
            }
            return fresh;
        }
    }

    private void write(JsonObject root) throws IOException {
        Path parent = configFile.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Path tmp = parent == null
                ? Files.createTempFile("openmcsurvey", ".tmp")
                : Files.createTempFile(parent, "openmcsurvey", ".tmp");
        try (Writer writer = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {
            GSON.toJson(root, writer);
        } catch (IOException | RuntimeException e) {
            try {
                Files.deleteIfExists(tmp);
            } catch (IOException ignored) {
            }
            throw e;
        }
        try {
            try {
                Files.move(tmp, configFile, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tmp, configFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException | RuntimeException e) {
            try {
                Files.deleteIfExists(tmp);
            } catch (IOException ignored) {
            }
            throw e;
        }
    }

    private static JsonObject defaults() {
        JsonObject root = new JsonObject();
        root.addProperty(OPT_IN, false);
        root.addProperty(FIRST_SENT, false);
        return root;
    }
}
