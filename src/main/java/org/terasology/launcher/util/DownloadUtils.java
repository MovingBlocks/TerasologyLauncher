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

package org.terasology.launcher.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.tasks.ProgressListener;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public final class DownloadUtils {

    private static final Logger logger = LoggerFactory.getLogger(DownloadUtils.class);

    private static final int CONNECT_TIMEOUT = 1000 * 30;
    private static final int READ_TIMEOUT = 1000 * 60 * 5;

    private DownloadUtils() {
    }

    public static void downloadToFile(URL downloadURL, Path file, ProgressListener listener) throws DownloadException {
        listener.update(0);

        final HttpURLConnection connection = getConnectedDownloadConnection(downloadURL);

        final long contentLength = connection.getContentLengthLong();
        if (contentLength <= 0) {
            throw new DownloadException("Wrong content length! URL=" + downloadURL + ", contentLength=" + contentLength);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Download file '{}' ({}; {}) from URL '{}'.", file, contentLength, connection.getContentType(), downloadURL);
        }

        try (BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
             BufferedOutputStream out = new BufferedOutputStream(Files.newOutputStream(file))) {
            downloadToFile(listener, contentLength, in, out);
        } catch (IOException e) {
            throw new DownloadException("Could not download file from URL! URL=" + downloadURL + ", file=" + file, e);
        } finally {
            connection.disconnect();
        }

        if (!listener.isCancelled()) {
            try {
                if (Files.size(file) != contentLength) {
                    throw new DownloadException("Wrong file length after download! " + Files.size(file) + " != " + contentLength);
                }
            } catch (IOException e) {
                throw new DownloadException("Failed to read the file length after download!", e);
            }
            listener.update(100);
        }
    }

    public static long getContentLength(URL downloadURL) throws DownloadException {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) downloadURL.openConnection();
            connection.setRequestMethod("HEAD");
            return connection.getContentLengthLong();
        } catch (IOException e) {
            throw new DownloadException("Could not send HEAD request to HTTP-URL! URL=" + downloadURL, e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static HttpURLConnection getConnectedDownloadConnection(URL downloadURL) throws DownloadException {
        final HttpURLConnection connection;
        try {
            connection = (HttpURLConnection) downloadURL.openConnection();
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.connect();
        } catch (ClassCastException | IOException e) {
            throw new DownloadException("Could not open/connect HTTP-URL connection! URL=" + downloadURL, e);
        }
        return connection;
    }

    private static void downloadToFile(ProgressListener listener, long contentLength, BufferedInputStream in,
                                       BufferedOutputStream out) throws IOException {
        final byte[] buffer = new byte[2048];
        final float sizeFactor = 100f / contentLength;
        long writtenBytes = 0;
        int n;
        if (!listener.isCancelled()) {
            while ((n = in.read(buffer)) != -1) {
                if (listener.isCancelled()) {
                    break;
                }

                out.write(buffer, 0, n);
                writtenBytes += n;

                int percentage = (int) (sizeFactor * writtenBytes);
                if (percentage < 1) {
                    percentage = 1;
                } else if (percentage >= 100) {
                    percentage = 99;
                }
                listener.update(percentage);

                if (listener.isCancelled()) {
                    break;
                }
            }
        }
    }
}
