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

import java.util.Date;

/**
 * Describes a Terasology module
 * @author Martin Steiger
 */
public class ModuleInfo {

    private String id;
    private String version;
    private String author;
    private String url;
    private String displayName;
    private String description;
    private int    stars;
    private Date   lastPush;

    public ModuleInfo() {
        // the default constructor is required for gson
    }

    public String getId() {
        return id;
    }

    public String getVersion() {
        return version;
    }

    public String getAuthor() {
        return author;
    }

    public String getUrl() {
        return url;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public int getStars() {
        return stars;
    }

    public Date getLastPush() {
        return lastPush;
    }

    @Override
    public String toString() {
        return "ModuleInfo['" + id + "', " + displayName + "(v" + version + ") by " + author + "]";
    }

}
