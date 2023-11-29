// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.game;

import com.google.common.base.MoreObjects;
import org.semver4j.Semver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.io.Installation;
import org.terasology.launcher.model.Build;
import org.terasology.launcher.model.GameIdentifier;
import org.terasology.launcher.model.Profile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Properties;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A local installation of a Terasology release.
 */
public class GameInstallation implements Installation<GameIdentifier> {

    private static final Logger logger = LoggerFactory.getLogger(GameInstallation.class);
    private final Path path;

    GameInstallation(Path installDirectory) {
        path = checkNotNull(installDirectory);
    }

    /**
     * Return an Installation after confirming it is present.
     */
    static GameInstallation getExisting(Path directory) throws FileNotFoundException {
        if (!Files.exists(directory)) {
            throw new FileNotFoundException("No installation present in " + directory);
        }
        return new GameInstallation(directory);
    }

    /**
     * The version of the Terasology engine used.
     *
     * @throws IOException           if an I/O error occurs
     * @throws FileNotFoundException if the engine or the version info could not be found
     */
    Semver getEngineVersion() throws IOException {
        return getEngineVersion(path);
    }

    /**
     * Locate the main game jar.
     * <p>
     * As of August 2021, Terasology has custom build logic to put libraries into a {@code libs} (plural) subdirectory.
     * As we plan to switch to using default Gradle behavior we have to do a quick check how the game distribution was
     * build (i.e., custom  {@code libs} or default {@code lib}).
     */
    Path getGameJarPath() throws IOException {
        return findJar(path, GameInstallation::matchGameJar, "game");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GameInstallation)) {
            return false;
        }

        GameInstallation that = (GameInstallation) o;

        return path.equals(that.path);
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("path", path)
                .toString();
    }

    static Path findJar(Path searchPath, BiPredicate<Path, BasicFileAttributes> predicate, String displayName) throws IOException {
        Path resultPath;

        try (var jarMatches = Files.find(searchPath, 3, predicate)) {
            var matches = jarMatches.collect(Collectors.toUnmodifiableSet());
            if (matches.isEmpty()) {
                throw new FileNotFoundException("Could not find " + displayName + " jar in " + searchPath);
            } else if (matches.size() > 1) {
                throw new FileNotFoundException(
                        String.format("Ambiguous results while looking for " + displayName + " jar in %s: %s",
                                searchPath, matches));
            }
            resultPath = matches.iterator().next();
        }
        return resultPath;
    }

    /**
     * Find the version of the Terasology engine installed here.
     * <p>
     * This method assumes that there is exactly one file named {@code engine*.jar} in the installation.
     *
     * @param versionDirectory the location containing the installation
     * @throws IOException           if an I/O error occurs
     * @throws FileNotFoundException if the engine or the version info could not be found
     */
    static Semver getEngineVersion(Path versionDirectory) throws IOException {
        Path engineJar = findJar(versionDirectory, GameInstallation::matchEngineJar, "engine");
        Properties versionInfo = getVersionPropertiesFromJar(engineJar);
        return new Semver(versionInfo.getProperty("engineVersion"));
    }

    /**
     * Lift a predicate taking the string form of the last component of a path to a
     * {@link java.util.function.BiPredicate BiPredicate} that can be used with
     * {@link Files#find(Path, int, BiPredicate, FileVisitOption...)}.
     * <p>
     * The resulting predicate automatically checks for the parent directory to be either {@code lib} or {@code libs}.
     * The passed in {@code predicate} will be called with the <i>string representation of the file name</i>, i.e., the
     * last element of each {@link Path}.
     *
     * @param predicate a predicate to match a file by file name
     * @return a predicate to match files by name under a {@code lib} or {@code libs} directory
     */
    static BiPredicate<Path, BasicFileAttributes> matchJar(Predicate<String> predicate) {
        return (path, basicFileAttributes) -> {
            final var libPaths = Set.of(Path.of("lib"), Path.of("libs"));

            var parent = path.getParent();
            var file = path.getFileName().toString();
            return Files.isDirectory(parent)
                    && libPaths.contains(parent.getFileName())
                    && predicate.test(file);
        };
    }

    /**
     * A {@link java.util.function.BiPredicate BiPredicate} matcher function for the Terasology Engine JAR file.
     * <p>
     * The Terasology Engine JAR file needs to be located within a {@code lib} or {@code libs} directory.
     * Its file name must be of the form {@code engine*.jar}.
     *
     * @param path                the path of the file to match
     * @param basicFileAttributes the file attributes of the file to match
     * @return true iff the file matches the expected pattern for Terasology's engine JAR
     */
    static boolean matchEngineJar(Path path, BasicFileAttributes basicFileAttributes) {
        return matchJar(file -> file.endsWith(".jar") && file.startsWith("engine")).test(path, basicFileAttributes);
    }

    static boolean matchGameJar(Path path, BasicFileAttributes basicFileAttributes) {
        return matchJar(file -> file.equals("Terasology.jar")).test(path, basicFileAttributes);
    }

    /**
     * Retrieve any {@code versionInfo.properties} from the given JAR file if it exists.
     * <p>
     * This method assumes that there is exactly one file named {@code versionInfo.properties} in the JAR.
     * Note, that this will try to parse <b>any</b> file matching the naming pattern into a {@link Properties} object
     * and return it.
     *
     * @param jarLocation the path to the JAR file containing a version info file
     * @return the version info properties object
     * @throws IOException           if an I/O error occurs
     * @throws FileNotFoundException if the version info file could not be found in the JAR file
     */
    static Properties getVersionPropertiesFromJar(Path jarLocation) throws IOException {
        try (var jar = new JarFile(jarLocation.toFile())) {
            var versionEntry = jar.stream().filter(entry ->
                    entry.getName().endsWith("versionInfo.properties")  // FIXME: use const
            ).findAny();
            if (versionEntry.isEmpty()) {
                throw new FileNotFoundException("Found no versionInfo.properties in " + jarLocation);
            }
            var properties = new Properties();
            try (var input = jar.getInputStream(versionEntry.get())) {
                properties.load(input);
            }
            return properties;
        }
    }

    @Override
    public Path getPath() {
        return path;
    }

    @Override
    public GameIdentifier getInfo() {
        //TODO: compute this information on instance creation (and fail creation in case it is not a valid installation)
        Profile profile;
        Build build;
        var parts = path.getNameCount();
        try {
            profile = Profile.valueOf(path.getName(parts - 3).toString());
            build = Build.valueOf(path.getName(parts - 2).toString());
            return new GameIdentifier(path.getFileName().toString(), build, profile);
        } catch (IllegalArgumentException e) {
            logger.debug("Cannot derive game information from installation: "
                    + "Expected directory format '.../<profile>/<build>/<game>' but got {}", path);
            return null;
        }
        //TODO: this is not working as I expected - probably don't fully understand this code I copied from somewhere else...
//        try {
//            Path jarPath = getGameJarPath();
//            Properties info = getVersionPropertiesFromJar(jarPath);
//            return new GameIdentifier(info.getProperty("displayVersion"), build, profile);
//        } catch (IOException e) {
//            logger.debug("Cannot derive game information from installation: "
//                    + "Cannot read 'displayVersion' from version info file for installation in {}.", path, e);
//            return null;
//        }
    }
}
