/* (C)2023 */
package org.transitclock.maintenance;

/**
 * Specifies how a file is archived. The implementers of the interface are for specific storage
 * types, such as AWS Glacier.
 *
 * @author SkiBu Smith
 */
public interface ArchiverInterface {

    /**
     * Called when file is to be uploaded to the archive.
     *
     * @param fileName Name of the file to be uploaded
     * @param description Description to be associated with the file being uploaded
     * @return the archive ID if successful, otherwise null
     */
    public abstract String upload(String fileName, String description);
}
