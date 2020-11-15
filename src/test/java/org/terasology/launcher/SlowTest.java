// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


/**
 * A test that is slower than normal.
 *
 * e.g. it uses external processes or other resources that take a little time to set up and
 * tear down.
 */
@Target({ METHOD, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Test
@Tag("slow")
public @interface SlowTest {
}
