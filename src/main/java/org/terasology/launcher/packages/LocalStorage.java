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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Provides game packages from the local filesystem.
 */
public class LocalStorage implements Storage {

    void install(GamePackage pkg) {
        // TODO: Implement this
    }

    void remove(GamePackage pkg) {
        // TODO: Implement this
    }

    @Override
    public List<Integer> getPackageVersions(GamePackageType pkgType) {
        // TODO: Implement this
        return Collections.emptyList();
    }

    @Override
    public Optional<GamePackage> getPackage(GamePackageType pkgType, int version) {
        // TODO: Implement this
        return Optional.empty();
    }
}
