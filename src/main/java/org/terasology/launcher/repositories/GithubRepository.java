// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.repositories;

import org.semver4j.Semver;
import org.semver4j.SemverException;
import okhttp3.OkHttpClient;
import org.kohsuke.github.GHAsset;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.HttpException;
import org.kohsuke.github.extras.okhttp3.OkHttpConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.game.GameVersionNotSupportedException;
import org.terasology.launcher.game.VersionHistory;
import org.terasology.launcher.model.Build;
import org.terasology.launcher.model.GameIdentifier;
import org.terasology.launcher.model.GameRelease;
import org.terasology.launcher.model.Profile;
import org.terasology.launcher.model.ReleaseMetadata;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class GithubRepository implements ReleaseRepository {

    private static final Logger logger = LoggerFactory.getLogger(GithubRepository.class);

    private GitHub github;

    public GithubRepository(final OkHttpClient httpClient) {
        try {
            github = GitHubBuilder.fromEnvironment()
                    .withConnector(new OkHttpConnector(httpClient))
                    .build();
            logger.debug("Github rate limit: {}", github.getRateLimit());
        } catch (HttpException e) {
            if (e.getResponseCode() != -1) {
                // if -1, no internet connection, do nothing, otherwise print stacktrace
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static GameRelease fromGithubRelease(GHRelease ghRelease) {
        final Profile profile = Profile.OMEGA;
        final Build build = ghRelease.isPrerelease() ? Build.NIGHTLY : Build.STABLE;
        final String tagName = ghRelease.getTagName();
        try {
            final Semver engineVersion;
            if (tagName.startsWith("v")) {
                engineVersion = new Semver(tagName.substring(1));
            } else {
                engineVersion = new Semver(tagName);
            }
            //TODO: check whether the launcher can fulfil this requirement
            final Semver minJavaVersion = VersionHistory.getJavaVersionForEngine(engineVersion);

            final Optional<GHAsset> gameAsset = ghRelease.assets().stream().filter(asset -> asset.getName().matches("Terasology.*zip")).findFirst();
            final URL url = new URL(gameAsset.map(GHAsset::getBrowserDownloadUrl).orElseThrow(() -> new IOException("Missing game asset.")));

            final String changelog = ghRelease.getBody();
            GameIdentifier id = new GameIdentifier(engineVersion.toString(), build, profile);

            ReleaseMetadata metadata = new ReleaseMetadata(changelog, ghRelease.getPublished_at());
            return new GameRelease(id, url, metadata);
        } catch (SemverException | IOException e) {
            logger.info("Could not create game release from Github release {}: {}",
                    ghRelease.getHtmlUrl(), e.getMessage());
        } catch (GameVersionNotSupportedException e) {
            logger.debug("Game release {} with engine version {} is not supported. ({})",
                    ghRelease.getHtmlUrl(), tagName, e.getMessage());
        }
        return null;
    }

    @Override
    public List<GameRelease> fetchReleases() {
        if (github != null) {
            try {
                final GHRepository repository = github.getRepository("MovingBlocks/Terasology");
                final List<GHRelease> githubReleases = repository.listReleases().toList();
                final List<GameRelease> releases = githubReleases.stream()
                        .map(GithubRepository::fromGithubRelease)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                logger.debug("Github rate limit: {}", github.getRateLimit());
                return releases;
            } catch (HttpException e) {
                if (e.getResponseCode() == -1) { // NOPMD
                    // no internet connection, do nothing
                } else {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return Collections.emptyList();
    }
}
