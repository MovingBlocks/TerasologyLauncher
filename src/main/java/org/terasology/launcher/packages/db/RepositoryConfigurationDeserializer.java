/*
 * Copyright 2020 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

public class RepositoryConfigurationDeserializer implements JsonDeserializer<RepositoryConfiguration> {
    @Override
    public RepositoryConfiguration deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        final RepositoryConfiguration repo = new RepositoryConfiguration();
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
