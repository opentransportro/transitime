/* (C)2023 */
package org.transitclock.maintenance;

import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.utils.Gzip;
import org.transitclock.utils.Time;
import org.transitclock.utils.Zip;

/**
 * For finding old files and archiving them. Used for log files.
 *
 * @author SkiBu Smith
 */
public class ArchiveOldFiles extends OldFileFinder {

    // Does the actual archiving
    private final ArchiverInterface archiver;

    private final String baseDirectory;

    private static final Logger logger = LoggerFactory.getLogger(ArchiveOldFiles.class);

    /********************** Member Functions **************************/

    /**
     * Private constructor.
     *
     * @param archiver Specifies the archiver to use to actually archive the file
     * @param baseDirectory Specifies directory where to find files. This part of the directory name
     *     is not included in the file description for each file in the archived zip file. This way
     *     the file names in the zip file will be something like "mbta/core/2014/12/20" instead of
     *     "D:/Logs/mbta/core/2014/12/20". The directory name must end with a "/".
     * @param subDirectory The subdirectory beyond the base directory. Specifies where to find the
     *     log files to be archived. This part of the file names is included in the file
     *     descriptions in the zip file.
     * @param daysOld If files older than daysOld found in a directory then that directory is
     *     archived.
     */
    private ArchiveOldFiles(ArchiverInterface archiver, String baseDirectory, String subDirectory, int daysOld) {
        super(daysOld);

        this.archiver = archiver;
        this.baseDirectory = baseDirectory;

        // Recursively handle directories
        recursivelyHandleDirectory(baseDirectory + subDirectory);
    }

    /**
     * Static method for instantiating class.
     *
     * @param archiver Specifies the archiver to use to actually archive the file
     * @param baseDirectory Specifies directory where to find files. This part of the directory name
     *     is not included in the file description for each file in the archived zip file. This way
     *     the file names in the zip file will be something like "mbta/core/2014/12/20" instead of
     *     "D:/Logs/mbta/core/2014/12/20".
     * @param subDirectory The subdirectory beyond the base directory. Specifies where to find the
     *     log files to be archived. This part of the file names is included in the file
     *     descriptions in the zip file.
     * @param daysOld If files older than daysOld found in a directory then that directory is
     *     archived.
     */
    public static void archive(ArchiverInterface archiver, String baseDirectory, String subDirectory, int daysOld) {
        // Make sure baseDirectory ends with a "/" so can append subDirectory
        // to get the full directory name.
        if (!baseDirectory.endsWith("/")) baseDirectory = baseDirectory + "/";

        new ArchiveOldFiles(archiver, baseDirectory, subDirectory, daysOld);
    }

    /**
     * Compresses specified file and deletes the original one.
     *
     * @param file
     * @return The new compressed File
     */
    private File gzipFile(File file) {
        logger.info("Compressing old file={}", file.getAbsoluteFile());

        // Compress the file, deleting the original
        String compressedFileName = Gzip.compress(file.getAbsolutePath());

        // Set last modified time of file to that of the original file for
        // consistency.
        long originalLastModifiedTime = file.lastModified();
        File compressedFile = new File(compressedFileName);
        compressedFile.setLastModified(originalLastModifiedTime);

        return compressedFile;
    }

    /**
     * Called for each file that is older than specified number of days. Compresses file if
     * necessary.
     *
     * <p>(non-Javadoc)
     *
     * @see org.transitclock.maintenance.OldFileFinder#handleOldFile(java.io.File)
     */
    @Override
    public void handleOldFile(File file) {
        logger.debug(
                "Processing old file={} which is {} days old.",
                file.getAbsolutePath(),
                (System.currentTimeMillis() - file.lastModified()) / Time.MS_PER_DAY);

        // Only need to process files that have content and that are not
        // zip or tar files
        if (file.length() != 0) {
            // Compress the file if it hasn't been compressed yet
            if (!Gzip.isGzipFile(file)
                    && !file.getName().endsWith(".tar")
                    && !file.getName().endsWith(".zip")) {
                file = gzipFile(file);
            }
        }
    }

    /**
     * Creates the zip file for the specified directory.
     *
     * @param directory
     * @return Full name of the created zip file
     */
    private String createZipFile(File directory) {
        String zipFileName = "dir.zip";
        String dirPath = directory.getAbsolutePath();
        String subDirectory = dirPath.substring(baseDirectory.length());
        String fullZipFileName = Zip.zip(baseDirectory, subDirectory, zipFileName);
        if (fullZipFileName == null) {
            logger.error("Failed creating zip file for directory \"{}\"", dirPath);
        }

        return fullZipFileName;
    }

    /**
     * Gets date from zipFileName path and returns it separated with '-' chars, which are more
     * appropriate for a date.
     *
     * @param zipFileName The date is obtained from the path of this file
     * @return The date in the format "2014-12-29"
     */
    private static String getDate(String zipFileName) {
        // Make sure always using forward slashes (Windows uses backwards ones)
        zipFileName = zipFileName.replace('\\', '/');

        int slashesFound = 0;
        String dateStr = zipFileName; // Default value if date not found
        for (int i = zipFileName.length() - 1; i >= 0; --i) {
            if (zipFileName.charAt(i) == '/') {
                ++slashesFound;
                if (slashesFound == 4) {
                    dateStr = zipFileName.substring(i + 1, zipFileName.lastIndexOf('/'));
                    break;
                }
            }
        }

        String dateWithoutSlashes = dateStr.replace('/', '-');
        return dateWithoutSlashes;
    }

    /**
     * Archives the zip file into cold storage in Amazon AWS Glacier
     *
     * @param fullZipFileName Name of the file to be archived
     * @return The upload ID in case file needs to be retrieved, null if not successful.
     */
    private String archiveZipFile(String fullZipFileName) {
        String archiveDescription = getDate(fullZipFileName);
        String uploadId = archiver.upload(fullZipFileName, archiveDescription);

        return uploadId;
    }

    /**
     * Recursively deletes all the files in the directory. This of course should only be called if
     * the files were successfully archived.
     *
     * @param directory Specifies directory of files to be deleted
     */
    private static void deleteFiles(File directory) {
        // For each file in the directory...
        File filesInDirectory[] = directory.listFiles();
        if (filesInDirectory == null) {
            logger.error(
                    "ArchiveOldFiles.deleteFiles() tried to delete old "
                            + "files from directory {} but listFiles() returned null.",
                    directory.getAbsoluteFile());
            return;
        }
        for (File fileOrDir : filesInDirectory) {
            // If it is a directory then recursively delete all of its files
            if (fileOrDir.isDirectory()) {
                deleteFiles(fileOrDir);
            }

            // Delete this file or directory
            fileOrDir.delete();
        }
    }

    /**
     * Zips up all files in directory, archives them, and then deletes all files in directory.
     * Called after directory with old file has been processed.
     *
     * <p>(non-Javadoc)
     *
     * @see org.transitclock.maintenance.OldFileFinder#handleDirectoryWithOldFile(java.io.File)
     */
    @Override
    protected void handleDirectoryWithOldFile(File directory) {
        logger.info("Archiving directory \"{}\" since it has an old file", directory.getAbsoluteFile());

        // Zip up the directory into a zip file
        String fullZipFileName = createZipFile(directory);
        if (fullZipFileName == null) return;

        // Archive zip file to cold storage
        String uploadId = archiveZipFile(fullZipFileName);
        if (uploadId == null) return;

        // Delete all the files in the directory, including the zip file,
        // since they have been successfully archived.
        deleteFiles(directory);
    }

    /**
     * Called after directory with old file has been processed. If directory is empty then deletes
     * it. This way the higher level directories, such as the year and month ones, are automatically
     * deleted when all the lower level files have been archived.
     *
     * <p>(non-Javadoc)
     *
     * @see org.transitclock.maintenance.OldFileFinder#handleDirectory(java.io.File)
     */
    @Override
    protected void handleDirectory(File directory) {
        File files[] = directory.listFiles();
        // If directory is empty then can delete it
        if (files != null && files.length == 0) directory.delete();
    }

    /**
     * For testing.
     *
     * @param args
     */
    public static void main(String[] args) {
        // Create an archiver that can write the files to Amazon AWS Glacier
        AwsGlacierArchiver archiver = new AwsGlacierArchiver(AwsGlacier.OREGON_REGION, "mbta-core", "D:/Logs/mbta");

        // Archive the files in the specified directory
        archive(archiver, "D:/Logs/", "mbta/core/2014/08", 120);
    }
}
