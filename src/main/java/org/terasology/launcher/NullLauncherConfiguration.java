/*
 * Copyright 2016 MovingBlocks
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

package org.terasology.launcher;

/*
 * An empty launcher configuration which can be used to purposely deal with empty configurations.
 *
 * For instance, the launcher self update process stops the configuration assemble process. Instead of returning <code>null</code> the
 * NullLauncherConfiguration can intentionally be returned.
 */
@Deprecated
public final class NullLauncherConfiguration extends LauncherConfiguration {

    private static NullLauncherConfiguration instance;

    private NullLauncherConfiguration() {
        super(null, null, null, null, null, null);
    }

    public static NullLauncherConfiguration getInstance() {
        if (instance == null) {
            instance = new NullLauncherConfiguration();
        }
        return instance;
    }
}
