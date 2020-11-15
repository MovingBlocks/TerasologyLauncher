// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher;

import org.hamcrest.Matcher;
import org.hamcrest.core.AllOf;
import org.hamcrest.core.IsIterableContaining;

import java.util.Collection;
import java.util.stream.Collectors;

public final class Matchers {

    private Matchers() { }

    /**
     * Creates a matcher for {@link Iterable}s that matches when consecutive passes over the
     * examined {@link Iterable} yield at least one item that is equal to the corresponding
     * item from the specified <code>items</code>.  Whilst matching, each traversal of the
     * examined {@link Iterable} will stop as soon as a matching item is found.
     * <p>
     * For example:
     * <p>
     * {@code
     * assertThat(Arrays.asList("foo", "bar", "baz"), hasItemsFrom(List.of("baz", "foo")))
     * }
     *
     * @param items
     *     the items to compare against the items provided by the examined {@link Iterable}
     */
    public static <T> Matcher<Iterable<? super T>> hasItemsFrom(Collection<T> items) {
        /* org.hamcrest.Matchers.hasItems(T...) takes variable arguments, so if
         * we want to match against a list, we reimplement it.
         */
        return new AllOf<>(items.stream().map(
                IsIterableContaining::hasItem
        ).collect(Collectors.toUnmodifiableList()));
    }
}
