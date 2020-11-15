// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.game;

import org.apache.commons.io.input.NullInputStream;
import org.terasology.launcher.StringIteratorInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.Callable;

public final class MockProcesses {
    static final Callable<Process> EXCEPTION_THROWING_START = () -> {
        throw new OurIOException("GRUMPY \uD83D\uDC7F");
    };

    private MockProcesses() {
    }

    public static class HappyGameProcess extends Process {

        private final InputStream inputStream;
        private long pid;

        HappyGameProcess() {
            inputStream = new NullInputStream(0);
        }

        HappyGameProcess(String processOutput) {
            inputStream = new ByteArrayInputStream(processOutput.getBytes());
        }

        @Override
        public OutputStream getOutputStream() {
            throw new UnsupportedOperationException("Stub.");
        }

        @Override
        public InputStream getInputStream() {
            return inputStream;
        }

        @Override
        public InputStream getErrorStream() {
            throw new UnsupportedOperationException("Stub; implement if we stop merging stdout and error streams.");
        }

        @Override
        public int waitFor() {
            return exitValue();
        }

        @Override
        public long pid() {
            if (this.pid == 0) {
                this.pid = new Random().nextLong();
            }
            return this.pid;
        }

        @Override
        public int exitValue() {
            return 0;
        }

        @Override
        public void destroy() {
            throw new UnsupportedOperationException();
        }
    }

    static class OneLineAtATimeProcess extends HappyGameProcess {
        private final Iterator<String> lines;

        OneLineAtATimeProcess(Iterator<String> lines) {
            this.lines = lines;
        }

        @Override
        public InputStream getInputStream() {
            // 🤔 If RunGameTask had a way to do
            //        Iterator<String> lines = adapt(process)
            //    and we could provide a different adapter for our mock process than a system
            //    process, we could probably do without StringIteratorInputStream. ?
            return new StringIteratorInputStream(lines);
        }
    }

    static class OurIOException extends IOException {
        OurIOException(final String grumpy) {
            super(grumpy);
        }
    }
}
