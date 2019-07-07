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

import java.util.List;
import java.util.Optional;

public class JenkinsStorage implements Storage {
    private static final String JENKINS_URL = "http://jenkins.terasology.org";

    @Override
    public List<GamePackage> getPackageList() {
        return null;
    }

    @Override
    public Optional<GamePackage> getPackage(String name) {
        return Optional.empty();
    }
}
