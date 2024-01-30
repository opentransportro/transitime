/* (C)2023 */
package org.transitclock.config.data;

import org.transitclock.config.IntegerConfigValue;
import org.transitclock.config.StringConfigValue;

/**
 * Config params for database
 *
 * @author SkiBu Smith
 */
public class DbSetupConfig {

    public static String getDbName() {
        return dbName.getValue();
    }

    private static StringConfigValue dbName = new StringConfigValue(
            "transitclock.db.dbName",
            null, // Null as default to use the projectId
            "Specifies the name of the database. If not set then the " + "transitclock.core.agencyId will be used.");

    public static String getDbHost() {
        return dbHost.getValue();
    }

    private static StringConfigValue dbHost = new StringConfigValue(
            "transitclock.db.dbHost",
            null, // Null as default so can get it from hibernate config
            "Specifies the name of the machine the database for the "
                    + "project resides on. Use null value to use values from "
                    + "hibernate config file. Set to \"localhost\" if database "
                    + "running on same machine as the core application.");

    public static String getDbType() {
        return dbType.getValue();
    }

    private static StringConfigValue dbType = new StringConfigValue(
            "transitclock.db.dbType",
            "postgres",
            "Specifies type of database when creating the URL to "
                    + "connect to the database. Can be mysql or postgresql. "
                    + "Should work for other dbs as well. Default is mysql.");

    public static String getDbUserName() {
        return dbUserName.getValue();
    }

    private static StringConfigValue dbUserName = new StringConfigValue(
            "transitclock.db.dbUserName",
            null,
            "Specifies login for the project database. Use null " + "value to use values from hibernate config file.");

    public static String getDbPassword() {
        return dbPassword.getValue();
    }

    private static StringConfigValue dbPassword = new StringConfigValue(
            "transitclock.db.dbPassword",
            null,
            "Specifies password for the project database. Use null "
                    + "value to use values from hibernate config file.",
            false); // Don't log password in configParams log file

    public static Integer getSocketTimeoutSec() {
        return socketTimeoutSec.getValue();
    }

    public static IntegerConfigValue socketTimeoutSec = new IntegerConfigValue(
            "transitclock.db.socketTimeoutSec",
            60,
            "So can set low-level socket timeout for JDBC connections. "
                    + "Useful for when a session dies during a request, such as "
                    + "for when a db is rebooted. Set to 0 to have no timeout.");

    /**
     * So that have flexibility with where the hibernate config file is. This way can easily access
     * it within Eclipse.
     *
     * @return
     */
    public static String getHibernateConfigFileName() {
        return hibernateConfigFileName.getValue();
    }

    private static StringConfigValue hibernateConfigFileName = new StringConfigValue(
            "transitclock.hibernate.configFile",
            "hibernate.cfg.xml",
            "Specifies the database dependent hibernate.cfg.xml file "
                    + "to use to configure hibernate. The system will look both "
                    + "on the file system and in the classpath. Can specify "
                    + "mysql_hibernate.cfg.xml or postgres_hibernate.cfg.xml");

    public static Integer getBatchSize() {
        return batchSize.getValue();
    }

    private static IntegerConfigValue batchSize = new IntegerConfigValue(
            "transitclock.db.batchSize", 100, "Specifies the database batch size, defaults to 100");




    public static final StringConfigValue encryptionPassword = new StringConfigValue(
            "transitclock.db.encryptionPassword",
            "SET THIS!",
            "Used for encrypting, deencrypting passwords for storage "
                    + "in a database. This value should be customized for each "
                    + "implementation and should be hidden from users.");
}
