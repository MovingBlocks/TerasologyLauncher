// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.repositories;

import org.kohsuke.github.GHAsset;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.model.Build;
import org.terasology.launcher.model.GameIdentifier;
import org.terasology.launcher.model.GameRelease;
import org.terasology.launcher.model.Profile;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class GithubRepositoryAdapter implements ReleaseRepository {

    private static final Logger logger = LoggerFactory.getLogger(GithubRepositoryAdapter.class);

    private GitHub github;

    public GithubRepositoryAdapter() {
        try {
            github = GitHub.connectAnonymously();
            logger.debug("Github rate limit: {}", github.getRateLimit());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static GameRelease fromGithubRelease(GHRelease ghRelease) {
        final Profile profile = Profile.OMEGA;
        final Build build = ghRelease.isPrerelease() ? Build.NIGHTLY : Build.STABLE;
        final String version = ghRelease.getTagName();

        try {
            final Optional<GHAsset> gameAsset = ghRelease.getAssets().stream().filter(asset -> asset.getName().matches("Terasology.*zip")).findFirst();
            final URL url = new URL(gameAsset.map(GHAsset::getBrowserDownloadUrl).orElse(null));

            final List<String> changelog = Arrays.asList(ghRelease.getBody().split("\n"));

            return new GameRelease(new GameIdentifier(version, build, profile), url, changelog, ghRelease.getPublished_at());
        } catch (IOException e) {
            logger.warn("Could not create game release from Github release {}", ghRelease.getHtmlUrl(), e);
            return null;
        }
    }

    @Override
    public List<GameRelease> fetchReleases() {
        if (github != null) {
            try {
                final GHRepository repository = github.getRepository("MovingBlocks/Terasology");
                final List<GHRelease> githubReleases = repository.listReleases().toList();
                final List<GameRelease> releases = githubReleases.stream()
                        .map(GithubRepositoryAdapter::fromGithubRelease)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                logger.debug("Github rate limit: {}", github.getRateLimit());
                return releases;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return Collections.emptyList();
    }
}
