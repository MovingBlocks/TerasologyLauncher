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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PackageManager {
    private final List<Storage> onlineStorages;
    private final LocalStorage localStorage;

    public PackageManager() {
        onlineStorages = new ArrayList<>();
        onlineStorages.add(new JenkinsStorage());

        localStorage = new LocalStorage();
    }

    public void sync() {
        // TODO: Update a cached list of available packages

        onlineStorages.forEach(storage -> System.out.println(storage.getPackageList()));
    }

    public void intstall(String name) {
        for (Storage storage : onlineStorages) {
            Optional<GamePackage> pkg = storage.getPackage(name);
            if (pkg.isPresent()) {
                localStorage.install(pkg.get());
                break;
            }
        }
    }

    public void remove(String name) {
        localStorage.getPackage(name)
                .ifPresent(localStorage::remove);
    }
}
