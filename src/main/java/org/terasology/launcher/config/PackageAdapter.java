/*
 * Copyright 2019 MovingBlocks
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

package org.terasology.launcher.config;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.terasology.launcher.packages.Package;

import java.io.IOException;
import java.util.Collections;

/**
 * Adds GSON support for {@link Package} instances.
 */
class PackageAdapter extends TypeAdapter<Package> {
    private static final String KEY_ID = "id";
    private static final String KEY_VERSION = "version";

    @Override
    public void write(JsonWriter out, Package pkg) throws IOException {
        if (pkg != null) {
            out.beginObject()
                    .name(KEY_ID)
                    .value(pkg.getId())
                    .name(KEY_VERSION)
                    .value(pkg.getVersion())
                    .endObject();
        } else {
            out.nullValue();
        }
    }

    @Override
    public Package read(JsonReader in) throws IOException {
        String id = null;
        String version = null;

        in.beginObject();
        while (in.hasNext()) {
            switch (in.nextName()) {
                case KEY_ID:
                    id = in.nextString();
                    break;
                case KEY_VERSION:
                    version = in.nextString();
                    break;
                default:
                    in.skipValue();
            }
        }
        in.endObject();

        return new Package(id, null, version, null, Collections.emptyList());
    }
}