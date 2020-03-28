package org.terasology.launcher.packages.db;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DatabaseRepositoryDeserializer implements JsonDeserializer<DatabaseRepository> {
    @Override
    public DatabaseRepository deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        final DatabaseRepository repo = new DatabaseRepository();
        final JsonObject obj = json.getAsJsonObject();

        repo.setUrl(obj.get("url").getAsString());
        repo.setType(obj.get("type").getAsString());

        final JsonArray pkgs = obj.getAsJsonArray("trackedPackages");
        final List<PackageMetadata> tracked = new ArrayList<>(pkgs.size());
        for (JsonElement e : pkgs) {
            final PackageMetadata metadata = new PackageMetadata();

            if (e.isJsonObject()) {
                // Newer schema
                JsonObject tmp = e.getAsJsonObject();
                metadata.setId(tmp.get("id").getAsString());
                metadata.setName(tmp.get("name").getAsString());
            } else if (e.isJsonPrimitive()) {
                // Older schema
                String tmp = e.getAsString();
                metadata.setId(tmp);
                metadata.setName(tmp);
            } else {
                throw new JsonParseException("Invalid format for \"trackedPackages\"");
            }
            tracked.add(metadata);
        }
        repo.setTrackedPackages(tracked.toArray(new PackageMetadata[0]));

        return repo;
    }
}
