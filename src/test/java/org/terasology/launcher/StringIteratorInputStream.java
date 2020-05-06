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

import com.google.common.primitives.Ints;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class StringIteratorInputStream extends InputStream {
    static final int COOLDOWN_LENGTH = 1;

    int cooldown;
    private final Iterator<String> source;
    private byte[] currentLine;
    private int byteIndex;

    public StringIteratorInputStream(Iterator<String> source) {
        this.source = source;
        resetCooldown();
    }

    @Override
    public int available() {
        if (currentLine == null) {
            // Playing hard-to-get with StreamDecoder.read. If it finishes a read
            // and still has room in its buffer, it'll check if we're ready again
            // right away. When we say we still aren't ready yet, it backs off.
            if (cooldown > 0) {
                cooldown -= 1;
                return 0;
            }
            if (!loadNextLine()) {
                return 0;
            }
        }
        return availableInCurrentLine();
    }

    @Override
    public int read() {
        if (currentLine == null) {
            var gotNext = loadNextLine();
            if (!gotNext) {
                return -1;
            }
        }
        var c = currentLine[byteIndex];
        byteIndex++;
        if (byteIndex >= currentLine.length) {
            currentLine = null;
            resetCooldown();
        }
        return c;
    }

    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        if (len == 0) {
            return 0;
        }
        // Even if available() says we're empty, our superclass wants us to try
        // to come at least one byte, blocking if necessary. Otherwise
        // StreamDecoder.readBytes says "Underlying input stream returned zero bytes"
        // and implodes.
        @SuppressWarnings("UnstableApiUsage") var availableLength =
                Ints.constrainToRange(availableInCurrentLine(), 1, len);
        return super.read(b, off, availableLength);
    }

    protected int availableInCurrentLine() {
        if (currentLine == null) {
            return 0;
        } else {
            return currentLine.length - byteIndex;
        }
    }

    private void resetCooldown() {
        cooldown = COOLDOWN_LENGTH;
    }

    private boolean loadNextLine() {
        try {
            final String nextString = source.next();
            currentLine = nextString.getBytes();
        } catch (NoSuchElementException e) {
            return false;
        } finally {
            byteIndex = 0;
        }
        return true;
    }
}
