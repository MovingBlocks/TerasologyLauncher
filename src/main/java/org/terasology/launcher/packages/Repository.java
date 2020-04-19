/*
 * Copyright 2019 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.launcher.packages;

import org.terasology.launcher.packages.db.RepositoryConfiguration;

import java.util.List;
import java.util.Optional;

/**
 * Interface for anything that can provide game packages.
 */
public interface Repository {
    List<Integer> getPackageVersions(PackageBuild pkgBuild);

    Optional<Package> getPackage(PackageBuild pkgBuild, int version);

    List<Package> getPackageList(RepositoryConfiguration config);
}
