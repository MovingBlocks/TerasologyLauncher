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