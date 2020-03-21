package org.terasology.launcher.github;

import javax.json.JsonObject;
import java.net.URI;

public class GitHubAsset {
    private final JsonObject json;

    public GitHubAsset(JsonObject json) {
        //TODO: validate JSON
        this.json = json;
    }

    public String getName() {
        return json.getString("name");
    }

    public URI getBrowserDownloadUrl() {
        return URI.create(json.getString("browser_download_url"));
    }
}
