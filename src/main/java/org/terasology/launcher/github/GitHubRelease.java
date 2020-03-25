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


package org.terasology.launcher.github;

import com.google.common.base.Preconditions;

import javax.json.JsonObject;

/**
 * Simplified interface for GitHub releases.
 *
 * See https://developer.github.com/v3/repos/releases/ for details.
 */
public class GitHubRelease {

    private final JsonObject json;

    /**
     * Create an instance from given JSON. Checks that all required fields are set.
     */
    public GitHubRelease(JsonObject json) {
        Preconditions.checkArgument(json.containsKey("body"), "Missing field: 'body'");
        Preconditions.checkArgument(json.containsKey("tag_name"), "Missing field: 'tag_name'");
        this.json = json;
    }

    public String getTagName() {
        return json.getString("tag_name");
    }

    public String getBody() {
        return json.getString("body");
    }
}
