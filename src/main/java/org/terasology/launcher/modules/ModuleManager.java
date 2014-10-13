/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.launcher.modules;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Parses module infos from a all-knowing json map
 * (as created by a python script in Jenkins).
 * @author Martin Steiger
 */
public class ModuleManager {

    private static final Logger logger = LoggerFactory.getLogger(ModuleManager.class);

    private final Gson gson = new Gson();

    private Map<String, ModuleInfo> modInfos = new HashMap<>();

    private final TypeToken<Map<String, ModuleInfo>> typeToken = new TypeToken<Map<String, ModuleInfo>>() {
            /* trick type erasure */
        };

    public ModuleManager(URL remoteUrl) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(remoteUrl.openStream(), Charset.forName("UTF-8"))) {
            Map<String, ModuleInfo> infos = gson.fromJson(reader, typeToken.getType());

            for (ModuleInfo info : infos.values()) {
                String id = info.getId();
                if (id != null) {
                    ModuleInfo prev = modInfos.put(id, info);
                    logger.debug("Found module info {}", id);
                    if (prev != null) {
                        logger.warn("ID {} already existing in database - overwriting", id);
                    }
                } else {
                    logger.warn("Encountered module info without id field");
                }
            }
        }
    }

    /**
     * @return an unmodifiable collection
     */
    public Collection<ModuleInfo> getAll() {
        return Collections.unmodifiableCollection(modInfos.values());
    }

    /**
     * @return the module info or <code>null</code>
     */
    public ModuleInfo getById(String id) {
        return modInfos.get(id);
    }
}
