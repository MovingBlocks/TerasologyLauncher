package org.terasology.launcher.github;

import com.jcabi.github.Github;
import com.jcabi.github.RtGithub;
import com.jcabi.http.Request;
import com.jcabi.http.response.JsonResponse;
import com.jcabi.http.wire.RetryWire;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.JsonObject;
import java.io.IOException;

/**
 * Simplified API for GitHub.
 * <p>
 * Expose a limited set of GitHub API features specific for the use in the launcher.
 */
public class GitHubClient {

    private static final Logger logger = LoggerFactory.getLogger(GitHubClient.class);

    private final Github github;

    /**
     * Instantiate a new client connecting to 'api.github.com'.
     */
    public GitHubClient() {
        github = new RtGithub(new RtGithub().entry().through(RetryWire.class));
    }

    /**
     * Send a {@code GET $path} to GitHub's REST API v3.
     * <p>
     * See https://developer.github.com/v3/ for more details.
     *
     * @param path any path that allows for GET request
     */
    JsonObject get(final String path) throws IOException {
        return github.entry()
                .method(Request.GET)
                .uri().path(path).back()
                .fetch()
                .as(JsonResponse.class)
                .json().readObject();
    }

    /**
     * Attempt to get the latest release via 'GET /repos/:owner/:repo/releases/latest'.
     * <p>
     * See https://developer.github.com/v3/repos/releases/#get-the-latest-release for more details.
     *
     * @param owner the organization or user owning the repository
     * @param repo  the name of the repository
     */
    //TODO: return Option<..> or Try<..> instead
    public GitHubRelease getLatestRelease(final String owner, final String repo) throws IOException {
        GitHubRelease release = null;
        try {
            release = new GitHubRelease(get(String.format("repos/%s/%s/releases/latest", owner, repo)));
        } catch (IllegalArgumentException e) {
            logger.info("Could not get latest release for '{}/{}'", owner, repo);
        }
        return release;
    }
}