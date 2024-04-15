/* (C)2023 */
package org.transitclock.utils;

import java.io.File;

import lombok.extern.slf4j.Slf4j;

/**
 * For grabbing a GTFS zip file over the web using http. The getFile() method copies the file to the
 * directory specified by the directory parameter or getDirectoryForStoringFile().
 *
 * @author SkiBu Smith
 */
@Slf4j
public class HttpGetGtfsFile extends HttpGetFile {

    /**
     * @param projectId For determining directory where to store file
     * @param urlStr URL of where to get file
     * @param directory where to put the retrieved file. If null then getDirectoryForStoringFile()
     *     is used as the directory.
     */
    public HttpGetGtfsFile(String projectId, String urlStr, String directory) {
        super(urlStr, directory != null ? directory : getDirectoryForStoringFile(projectId));
    }

    /**
     * Returns directory name of where to store the file. The directory will be
     * /USER-HOME/gtfs/projectId/MM-dd-yyyy/
     *
     * @return the directory name for storing the results
     */
    private static String getDirectoryForStoringFile(String projectId) {
        return System.getProperty("user.home")
                + "/gtfs/"
                + projectId
                + "/"
                + Time.dateStr(System.currentTimeMillis())
                + "/";
    }

    /**
     * Main entry point to class. Reads in specified file from URL and stores it using same file
     * name into directory specified by getDirectoryForFile(). The directory name will be something
     * like "~/gtfs/projectId/MM-dd-yyyy".
     *
     * @param projectId For determining directory where to store file
     * @param urlStr URL of where to get file
     * @param directory where to put the retrieved file. If null then getDirectoryForStoringFile()
     *     is used as the directory.
     * @return The file name of the newly created file, null if there was a problem
     */
    public static String getFile(String projectId, String urlStr, String directory) {
        HttpGetFile getter = new HttpGetGtfsFile(projectId, urlStr, directory);
        try {
            getter.getFile();
            return getter.getFullFileName();
        } catch (Exception e) {
            logger.error("Exception occurred when reading in file: {}", e.getMessage(), e);
            return null;
        }
    }
}
