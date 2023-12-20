/* (C)2023 */
package org.transitclock.maintenance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.utils.Time;

import java.io.File;
import java.io.IOException;

/**
 * Abstract class for finding files older than specified number of days. The implementing class
 * overrides the handleFile(File) method to actually do something with the file.
 *
 * @author SkiBu Smith
 */
public abstract class OldFileFinder {

    private final long oldFileEpochTime;

    private static final Logger logger = LoggerFactory.getLogger(OldFileFinder.class);

    /********************** Member Functions **************************/

    /**
     * Constructor. Recursively goes through specified directory and finds files older than
     * specified number of days and calls the abstract method handleFile() on them.
     *
     * @param directoryRootName
     * @param daysOld
     */
    public OldFileFinder(int daysOld) {
        oldFileEpochTime = System.currentTimeMillis() - daysOld * Time.MS_PER_DAY;
    }

    /**
     * Recursively goes through the directory.
     *
     * @param directoryName
     * @throws IOException
     */
    protected void recursivelyHandleDirectory(String directoryName) {
        File directory = new File(directoryName);
        if (!directory.exists()) {
            logger.error("Directory \"{}\" does not exist.", directoryName);
            return;
        }

        boolean oldFileFoundInDirectory = false;

        // For each file in the directory...
        File filesInDirectory[] = directory.listFiles();
        for (File file : filesInDirectory) {
            // Handle depending on whether a directory
            if (file.isDirectory()) {
                // Handle directories recursively
                try {
                    recursivelyHandleDirectory(file.getCanonicalPath());
                } catch (IOException e) {
                    logger.error("Exception while recursively handling " + "directory \"{}\"", directoryName);
                }
            } else {
                // Handle file if it is old
                if (file.lastModified() < oldFileEpochTime) {
                    handleOldFile(file);
                    oldFileFoundInDirectory = true;
                }
            }
        }

        // If found old file in directory call abstract method to handle this
        // directory, such as for archiving all the files.
        if (oldFileFoundInDirectory) handleDirectoryWithOldFile(directory);

        // Done handling this directory so call overridden method
        handleDirectory(directory);
    }

    /**
     * Abstract method to be overridden. Called for each file (not for directories).
     *
     * @param file
     */
    protected abstract void handleOldFile(File file);

    /**
     * Abstract method to be overridden. Called for each directory that has an old file after
     * handleFile() is called on each file in directory.
     *
     * @param directory
     */
    protected abstract void handleDirectoryWithOldFile(File directory);

    /**
     * Abstract method to be overridden. Called for each directory processed after handleFile() has
     * been called on each file and handleDirectoryWithOldFile() has been called for directories
     * with old files.
     *
     * @param directory
     */
    protected abstract void handleDirectory(File directory);
}
