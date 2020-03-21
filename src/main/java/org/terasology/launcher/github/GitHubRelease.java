package org.terasology.launcher.github;

import com.google.common.collect.ImmutableList;

import javax.json.JsonObject;
import javax.json.JsonValue;

public class GitHubRelease {

    private final JsonObject json;

    public GitHubRelease(JsonObject json) {
        //TODO: validate JSON
        this.json = json;
    }

    public String getTagName() {
        return json.getString("tag_name");
    }

    public String getBody() {
        return json.getString("body");
    }

    public ImmutableList<GitHubAsset> getAssets() {
        return json.getJsonArray("assets").stream()
                .map(JsonValue::asJsonObject)
                .map(GitHubAsset::new)
                .collect(ImmutableList.toImmutableList());
    }
}
