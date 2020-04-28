/*
 * Copyright 2020 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.launcher;

import org.hamcrest.Matcher;
import org.hamcrest.core.AllOf;
import org.hamcrest.core.IsIterableContaining;

import java.util.Collection;
import java.util.stream.Collectors;

public final class Matchers {

    private Matchers() { }

    public static <T> Matcher<Iterable<? super T>> hasItemsFrom(Collection<T> items) {
        /* org.hamcrest.Matchers.hasItems(T...) takes variable arguments, so if
         * we want to match against a list, we reimplement it.
         */
        return new AllOf<>(items.stream().map(
                IsIterableContaining::hasItem
        ).collect(Collectors.toUnmodifiableList()));
    }
}
