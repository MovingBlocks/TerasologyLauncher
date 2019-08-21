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

/**
 * Handles fetching of packages from any custom repository.
 * Should be used to handle any third party repository.
 */
class CustomRepositoryHandler implements RepositoryHandler {
    @Override
    public List<Package> getPackages(PackageDatabase.Repository source) {
        // TODO: Fetch packages using a specific pattern

        return Collections.emptyList();
    }
}
