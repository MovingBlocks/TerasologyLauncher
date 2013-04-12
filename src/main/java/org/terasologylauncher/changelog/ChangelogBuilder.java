/*
 * Copyright (c) 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasologylauncher.changelog;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * @author MrBarsack
 */

public class ChangelogBuilder {


    public String getChangelog(Document changelog, int build) {
        NodeList nodeList = null;

        if (changelog != null) {
            nodeList = changelog.getElementsByTagName("msg");
        }

        StringBuilder str = new StringBuilder("<b> Build:");

        str.append(build).append("</b> <br/>");

        assert nodeList != null;

        for (int a = 0; a < nodeList.getLength(); a++) {
            str.append('-').append(nodeList.item(a).getLastChild().getTextContent()).append("<br/>");
        }
        str.append("<br/>");

        return str.toString();
    }
}
