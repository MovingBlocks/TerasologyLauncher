// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.game;

import com.google.common.base.MoreObjects;
import com.vdurmont.semver4j.Semver;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/** A local installation of a Terasology release. */
class Installation {
    final Path path;

    Installation(Path installDirectory) {
        path = checkNotNull(installDirectory);
    }

    /**
     * The version of the Terasology engine used.
     *
     * @throws IOException if an I/O error occurs
     * @throws FileNotFoundException if the engine or the version info could not be found
     */
    Semver getEngineVersion() throws IOException {
        return getEngineVersion(path);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Installation)) {
            return false;
        }

        Installation that = (Installation) o;

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

    /**
     * Find the version of the Terasology engine installed here.
     * <p>
     * This method assumes that there is exactly one file named {@code engine*.jar} in the installation.
     *
     * @param versionDirectory the location containing the installation
     * @throws IOException if an I/O error occurs
     * @throws FileNotFoundException if the engine or the version info could not be found
     */
    static Semver getEngineVersion(Path versionDirectory) throws IOException {
        Path engineJar;

        try (var jarMatches = Files.find(versionDirectory, 3, Installation::matchEngineJar)) {
            var matches = jarMatches.collect(Collectors.toUnmodifiableSet());
            if (matches.isEmpty()) {
                throw new FileNotFoundException("Could not find engine jar in " + versionDirectory);
            } else if (matches.size() > 1) {
                throw new FileNotFoundException(
                        String.format("Ambiguous results while looking for engine jar in %s: %s",
                                versionDirectory, matches));
            }
            engineJar = matches.iterator().next();
        }

        Properties versionInfo = getVersionPropertiesFromJar(engineJar);

        return new Semver(versionInfo.getProperty("engineVersion"), Semver.SemverType.IVY);
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
        // Are there path-matching utilities to simplify this?
        final var libPaths = Set.of(Path.of("lib"), Path.of("libs"));

        var parent = path.getParent();
        var file = path.getFileName().toString();
        return Files.isDirectory(parent)
                && libPaths.contains(parent.getFileName())
                && file.endsWith(".jar")
                && file.startsWith("engine");
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
     * @throws IOException if an I/O error occurs
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
}
