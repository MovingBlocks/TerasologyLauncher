// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.tasks.ProgressListener;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public final class DownloadUtils {

    private static final Logger logger = LoggerFactory.getLogger(DownloadUtils.class);

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration READ_TIMEOUT = Duration.ofMinutes(5);

    private DownloadUtils() {
    }

    public static CompletableFuture<Void> downloadToFile(URL downloadURL, Path file, ProgressListener listener) throws DownloadException {
        listener.update(0);

        var result = getConnectedDownloadConnection(downloadURL);

        return result.thenAcceptAsync(response -> {
            var contentLength = response.headers().firstValueAsLong("content-length").orElse(0L);
            logger.debug("Download file '{}' ({}; {}) from URL '{}'.", file, contentLength,
                    response.headers().firstValue("content-type"), downloadURL);

            try (BufferedInputStream in = new BufferedInputStream(response.body());
                 BufferedOutputStream out = new BufferedOutputStream(Files.newOutputStream(file))) {
                downloadToFile(listener, contentLength, in, out);
            } catch (IOException e) {
                throw new DownloadException("Could not download file from URL! URL=" + downloadURL + ", file=" + file, e);
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
        });
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

    private static CompletableFuture<HttpResponse<InputStream>> getConnectedDownloadConnection(URL downloadURL) throws DownloadException {
        var client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(CONNECT_TIMEOUT)
                .build();

        HttpRequest request;
        try {
            request = HttpRequest.newBuilder(downloadURL.toURI()).timeout(READ_TIMEOUT).build();
        } catch (URISyntaxException e) {
            throw new DownloadException("Error in URL: " + downloadURL, e);
        }
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream());
    }

    private static void downloadToFile(ProgressListener listener, long contentLength, BufferedInputStream in,
                                       BufferedOutputStream out) throws IOException {
        final byte[] buffer = new byte[2048];
        final float sizeFactor = 100f / contentLength;
        long writtenBytes = 0;
        int n;
        if (!listener.isCancelled()) {
            while ((n = in.read(buffer)) != -1) { //NOPMD(AssignmentInOperand)
                if (listener.isCancelled()) {
                    break;
                }

                out.write(buffer, 0, n);
                writtenBytes += n;

                int percentage = (int) (sizeFactor * writtenBytes);
                if (percentage < 1) {
                    percentage = 1;
                } else if (percentage >= 100) { //NOPMD(AvoidLiteralsInIfCondition)
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
