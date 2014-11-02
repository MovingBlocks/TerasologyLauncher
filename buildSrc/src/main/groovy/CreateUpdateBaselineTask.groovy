import groovy.json.JsonOutput
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileTreeElement
import org.gradle.api.tasks.TaskAction
import org.tukaani.xz.LZMA2Options
import org.tukaani.xz.XZOutputStream

import java.security.MessageDigest

/*
 * Copyright 2014 MovingBlocks
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

class CreateUpdateBaselineTask extends DefaultTask {

    def File inputDir;

    def File outputDir;

    def String productId

    @TaskAction
    void createUpdateBaseline() {

        if (outputDir.exists()) {
            println "Output directory already exists. Cleaning."
            outputDir.deleteDir()
        }
        outputDir.mkdirs()

        def manifest = [
                version: 1,
                created: new Date().time,
                product: productId,
                files  : []
        ]

        def digest = MessageDigest.getInstance("SHA-256")

        println "Compressing distribution files and creating manifest..."

        this.project.fileTree(inputDir).visit {
            FileTreeElement file ->

                if (file.directory || file.relativePath.toString() == "manifest.json") {
                    return;
                }

                digest.reset();
                // Calculate hash first
                file.file.eachByte(1024 * 1024) { byte[] buf, int read ->
                    digest.update(buf, 0, read)
                }
                def hashValue = digest.digest().encodeHex().toString()

                File outFile = new File(outputDir, hashValue + ".xz")
                if (!outFile.exists()) {
                    def options = new LZMA2Options(LZMA2Options.PRESET_DEFAULT)
                    new XZOutputStream(new FileOutputStream(outFile), options).withStream { OutputStream stream ->
                        file.copyTo stream
                    }
                }

                manifest.files.add([
                    path: file.relativePath.toString(),
                    hash: hashValue,
                    size: file.size,
                    compressedSize: outFile.size()
                ])

        }

        String manifestJson = JsonOutput.prettyPrint(JsonOutput.toJson(manifest))

        // Write manifest to source directory
        new File(inputDir, "manifest.json").withWriter("utf-8") {
            w -> w.print(manifestJson)
        }

        // Write manifest to target directory
        new File(outputDir, "manifest.json.xz").withOutputStream {
            OutputStream out ->
                def options = new LZMA2Options(LZMA2Options.PRESET_DEFAULT)
                new XZOutputStream(out, options).withWriter("utf-8") {
                    Writer w -> w.print(manifestJson)
                }
        }

    }

}
