package org.terasology.launcher.github;

import com.jcabi.github.Github;
import com.jcabi.github.RtGithub;
import com.jcabi.http.Request;
import com.jcabi.http.response.JsonResponse;
import com.jcabi.http.wire.RetryWire;

import javax.json.JsonObject;
import java.io.IOException;

public class GitHubClient {

    private final Github github;

    public GitHubClient() {
        github = new RtGithub(new RtGithub().entry().through(RetryWire.class));
    }

    public JsonObject get(String path) throws IOException {
        return github.entry()
                .method(Request.GET)
                .uri().path(path).back()
                .fetch()
                .as(JsonResponse.class)
                .json().readObject();
    }
}
