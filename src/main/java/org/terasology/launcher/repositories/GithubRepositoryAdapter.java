// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.repositories;

import com.vdurmont.semver4j.Semver;
import com.vdurmont.semver4j.SemverException;
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
import org.terasology.launcher.model.ReleaseMetadata;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class GithubRepositoryAdapter implements ReleaseRepository {

    private static final Logger logger = LoggerFactory.getLogger(GithubRepositoryAdapter.class);

    /**
     * The preview release of v4.1.0-rc.1 is the first release with LWJGL v3.
     * See https://github.com/MovingBlocks/Terasology/releases/tag/v4.1.0-rc.1
     */
    private static final Semver FIRST_LWJGL3_RELEASE = new Semver("4.1.0-rc.1");

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
        final String tagName = ghRelease.getTagName();
        try {
            final Semver engineVersion;
            if (tagName.startsWith("v")) {
                engineVersion = new Semver(tagName.substring(1));
            } else {
                engineVersion = new Semver(tagName);
            }

            final Optional<GHAsset> gameAsset = ghRelease.assets().stream().filter(asset -> asset.getName().matches("Terasology.*zip")).findFirst();
            final URL url = new URL(gameAsset.map(GHAsset::getBrowserDownloadUrl).orElseThrow(() -> new IOException("Missing game asset.")));

            final String changelog = ghRelease.getBody();
            GameIdentifier id = new GameIdentifier(engineVersion.toString(), engineVersion, build, profile);

            boolean isLwjgl3 = engineVersion.isGreaterThanOrEqualTo(FIRST_LWJGL3_RELEASE);
            ReleaseMetadata metadata = new ReleaseMetadata(changelog, ghRelease.getPublished_at(), isLwjgl3);
            return new GameRelease(id, url, metadata);
        } catch (SemverException | IOException e) {
            logger.info("Could not create game release from Github release {}: {}", ghRelease.getHtmlUrl(), e.getMessage());
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
