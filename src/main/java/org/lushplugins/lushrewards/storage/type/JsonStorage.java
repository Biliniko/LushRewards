package org.lushplugins.lushrewards.storage.type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.lushplugins.lushrewards.LushRewards;
import org.lushplugins.lushrewards.storage.Storage;

import java.io.*;
import java.util.UUID;

public class JsonStorage implements Storage {
    private final File storageDir = new File(LushRewards.getInstance().getDataFolder(), "data");

    @Override
    public JsonObject loadModuleUserDataJson(UUID uuid, String moduleId) {
        String path = moduleId != null ? moduleId : "main";

        JsonObject json = loadFile(uuid);
        return json.has(path) ? json.get(path).getAsJsonObject() : null;
    }

    @Override
    public void saveModuleUserDataJson(UUID uuid, String moduleId, JsonObject moduleJson) {
        assertStorageDir();

        if (moduleJson == null) {
            throw new NullPointerException("JsonObject cannot be null when saving");
        }

        JsonObject json = loadFile(uuid);
        json.add(moduleId != null ? moduleId : "main", moduleJson);
        try {
            FileWriter writer = new FileWriter(getUserFile(uuid));
            LushRewards.GSON.toJson(json, writer);
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void assertStorageDir() {
        if (!storageDir.exists()) {
            storageDir.mkdir();
        }
    }

    private JsonObject loadFile(UUID uuid) {
        assertStorageDir();

        try {
            JsonElement json = JsonParser.parseReader(new FileReader(getUserFile(uuid)));
            return json.isJsonObject() ? json.getAsJsonObject() : new JsonObject();
        } catch (FileNotFoundException e) {
            return new JsonObject();
        }
    }

    private File getUserFile(UUID uuid) {
        return new File(storageDir, uuid + ".json");
    }
}
