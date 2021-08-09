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
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GithubRepositoryAdapter implements ReleaseRepository {

    private static final Logger logger = LoggerFactory.getLogger(GithubRepositoryAdapter.class);
    private static final Pattern NAME_REGEX = Pattern.compile(
            "(.*?Engine\\s+)?v?(?<engine>\\d+\\.\\d+.*?)\\s*[/-]\\s*(?<display>.*)",
            Pattern.CASE_INSENSITIVE);

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
        final String displayVersion;
        var nameMatch = NAME_REGEX.matcher(ghRelease.getName());
        if (nameMatch.matches()) {
            displayVersion = nameMatch.group("display")
                    .toLowerCase(Locale.ROOT)
                    .replaceAll("[\\s-]+", "-");
        } else {
            displayVersion = ghRelease.getName();
        }

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
            GameIdentifier id = new GameIdentifier(displayVersion, engineVersion, build, profile);

            ReleaseMetadata metadata = new ReleaseMetadata(changelog, ghRelease.getPublished_at());
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
