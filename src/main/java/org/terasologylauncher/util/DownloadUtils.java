/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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

package org.terasologylauncher.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public class DownloadUtils {

    /**
     * Download the file from the given URL and store it to the specified file.
     *
     * @param downloadURL - remote location of file to download
     * @param file        - where to store downloaded file
     *
     * @throws IOException
     */
    public static void downloadToFile(URL downloadURL, File file) throws IOException {
        InputStream in = downloadURL.openStream();
        OutputStream out = new FileOutputStream(file);
        final byte[] buffer = new byte[2048];

        int n;
        while ((n = in.read(buffer)) != -1) {
            out.write(buffer, 0, n);
        }

        if (in != null) {
            in.close();
        }
        if (out != null) {
            out.close();
        }
    }
}
