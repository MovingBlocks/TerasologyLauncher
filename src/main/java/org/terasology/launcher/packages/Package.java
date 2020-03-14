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

package org.terasology.launcher.packages;

import java.io.Serializable;
import java.util.List;

/**
 * Model of a package handled by the PackageManager.
 */
public class Package implements Serializable {
    private final String id;
    private final String name;
    private final String version;
    private final String url;
    private final List<String> changelog;
    private boolean installed;

    public Package(String id, String name, String version, String url, List<String> changelog) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.url = url;
        this.changelog = changelog;
        installed = false;
    }

    public String zipName() {
        return id + "-" + version + ".zip";
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getUrl() {
        return url;
    }

    public List<String> getChangelog() {
        return changelog;
    }

    public boolean isInstalled() {
        return installed;
    }

    public void setInstalled(boolean installed) {
        this.installed = installed;
    }
}
