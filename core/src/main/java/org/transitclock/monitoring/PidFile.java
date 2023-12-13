/* (C)2023 */
package org.transitclock.monitoring;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * For creating a PID file. Can be used by monit to automatically start process when server rebooted
 * or if application crashes.
 *
 * @author Michael
 */
public class PidFile {

    private static final Logger logger = LoggerFactory.getLogger(PidFile.class);

    /**
     * Returns the PID of the process. Not guaranteed to be fully portable.
     *
     * @return The PID of the process.
     */
    public static String getPid() {
        final String jvmName = ManagementFactory.getRuntimeMXBean().getName();
        final int ampersand = jvmName.indexOf('@');
        if (ampersand < 1) return null;

        return jvmName.substring(0, ampersand);
    }

    /**
     * Writes the PID of the process into the specified file.
     *
     * @param fileName Name of PID file to write
     */
    public static void createPidFile(String fileName) {
        // Determine the pid for the process
        String pid = getPid();

        try {
            // Create directory in case it doesn't already exist
            Path path = Paths.get(fileName);
            Files.createDirectories(path.getParent());

            // Create the pid file and write the pid to it
            OutputStream out =
                    Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            out.write(pid.getBytes());
            out.close();
        } catch (IOException e) {
            logger.error("Could not create PID file. {}", e.getMessage());
        }
    }
}
