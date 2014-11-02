import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.nio.file.StandardOpenOption
import java.util.zip.ZipFile

/**
 * Created by Sebastian on 02.11.2014.
 */

class ExtractServerVmTask extends DefaultTask {

    @Input
    File jdkInstaller

    @OutputDirectory
    File outputDir

    @TaskAction
    public void run() {

        if (!outputDir.exists())
            outputDir.mkdirs()

        byte[] expectedSig = ['M', 'S', 'C', 'F', 0, 0, 0, 0];

        def channel = FileChannel.open(jdkInstaller.toPath(), StandardOpenOption.READ)
        def buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size())
        buffer.order(ByteOrder.LITTLE_ENDIAN)

        int offset = 0;
        while (buffer.position() < buffer.limit()) {
            byte[] sigBuffer = new byte[8];
            buffer.position(offset);
            buffer.get(sigBuffer);
            if (Arrays.equals(sigBuffer, expectedSig)) {
                int cabSize = buffer.getInt()
                println "Found cabinet @ $offset, Size: $cabSize"

                File tempDir = temporaryDir

                try {
                    File tempCabFile = new File(tempDir, "temp.cab")

                    tempCabFile.withOutputStream { stream ->
                        int remaining = cabSize;
                        buffer.position(offset);
                        byte[] b = new byte[1024 * 1024];
                        while (remaining > 0) {
                            int toRead = Math.min(remaining, b.length)
                            buffer.get(b, 0, toRead)
                            remaining -= toRead
                            stream.write(b, 0, toRead);
                        }
                    }

                    offset += cabSize

                    String[] cmdArgs;
                    if (!Os.isFamily(Os.FAMILY_WINDOWS)) {
                        File cabExtract = new File(project.projectDir, "tools/cabextract")
                        cabExtract.setExecutable(true)
                        cmdArgs = [
                            cabExtract.getAbsolutePath(),
                            tempCabFile.getAbsolutePath(),
                            "--filter",
                            "tools.zip",
                            "--directory",
                            tempDir.getAbsolutePath()
                        ];
                    } else {
                        cmdArgs = [
                                "expand",
                                "-R", // otherwise expand will not rename the files to their original filename
                                tempCabFile.getAbsolutePath(),
                                "-F:tools.zip",
                                tempDir.getAbsolutePath()
                        ];
                    }
                    Process process = Runtime.getRuntime().exec(cmdArgs);
                    int result = process.waitFor();
                    println "Result of expansion: $result"

                    File toolsZip = new File(tempDir, "tools.zip");
                    if (toolsZip.exists()) {
                        println "Found tools.zip!"
                        ZipFile zf = new ZipFile(toolsZip);
                        def ze = zf.getEntry("jre/bin/server/jvm.dll")
                        if (ze == null) {
                            throw new RuntimeException("tools.zip does not contain server VM!")
                        }
                        zf.getInputStream(ze).withStream { stream ->
                            new File(outputDir, "jvm.dll").withOutputStream { output ->
                                output << stream
                            }
                        }
                        ze = zf.getEntry("jre/bin/server/Xusage.txt")
                        if (ze != null) {
                            zf.getInputStream(ze).withStream { stream ->
                                new File(outputDir, "Xusage.txt").withOutputStream { output ->
                                    output << stream
                                }
                            }
                        }

                        zf.close()
                        return
                    }
                } finally {
                    tempDir.deleteDir()
                }

            } else {
                offset++;
            }
        }

        channel.close();

        throw new RuntimeException("Could not find server VM in JDK installer!");

    }

}
