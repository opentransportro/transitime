/* (C)2023 */
package org.transitclock.applications;

import com.google.common.reflect.ClassPath;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Types;
import java.util.*;

/**
 * For generating SQL schema files based on classes to be stored in database
 * that were annotated for Hibernate. This is much nicer than trying to figure
 * out what the schema really should be by hand. This code was copied from
 * http:/
 * /jandrewthompson.blogspot.nl/2009/10/how-to-generate-ddl-scripts-from.html
 * <p>
 * Note that unfortunately there does not appear to be a way to specify the
 * order of the columns in the resulting create table SQL statements. Though
 * this has been asked for, it appears to still be a limitation of Hibernate.
 * The default ordering appears to be first the @Id columns in reverse
 * alphabetical order, and then the non @Id columns in alphabetical order. Yes,
 * quite peculiar.
 * <p>
 * Since the resulting automatically generated files have unneeded drop commands
 * these are filtered out. This way the resulting sql is smaller and easier to
 * understand.
 *
 * @author john.thompson, Skibu Smith, and Sean Crudden
 *
 */
@Slf4j
public class SchemaGenerator {
    private final List<Class<Object>> classList = new ArrayList<>();
    private final String packageName;
    private final String outputDirectory;


    /**
     * MySQL handles fractional seconds differently from PostGRES and other
     * DBs. Need to use "datetime(3)" for fractional seconds whereas with
     * PostGRES can use the default "timestamp" type. In order to handle
     * this properly in the generated ddl schema files need to not use
     * @Column(columnDefinition="datetime(3)") in the Java class that defines
     * the db object. Instead need to use this special ImprovedMySQLDialect
     * as the Dialect.
     */
    public static class ImprovedMySQLDialect extends MySQLDialect {
        public ImprovedMySQLDialect() {
            super();
            // Specify special SQL type for MySQL for timestamps so that get
            // fractions seconds.
            registerColumnType(Types.TIMESTAMP, "datetime(3)");
        }
    }

    @SuppressWarnings("unchecked")
    public SchemaGenerator(String packageName, String outputDirectory) throws Exception {
        for (Class<Object> clazz : getClasses(packageName)) {
            classList.add(clazz);
        }

        this.packageName = packageName;
        this.outputDirectory = outputDirectory;
    }

    /**
     * Gets rid of the unwanted drop table commands. These aren't needed because
     * the resulting script is intended only for creating a database, not for
     * deleting all the data and recreating the tables.
     *
     * @param outputFilename
     */
    private void trimCruftFromFile(String outputFilename) {
        // Need to write to a temp file because if try to read and write
        // to same file things get quite confused.
        String tmpFileName = outputFilename + "_tmp";

        BufferedReader reader = null;
        BufferedWriter writer = null;
        try {
            FileInputStream fis = new FileInputStream(outputFilename);
            reader = new BufferedReader(new InputStreamReader(fis));

            FileOutputStream fos = new FileOutputStream(tmpFileName);
            writer = new BufferedWriter(new OutputStreamWriter(fos));

            String line;
            while ((line = reader.readLine()) != null) {
                // Filter out "drop table" commands
                if (line.contains("drop table")) {
                    // Read in following blank line
                    line = reader.readLine();

                    // Continue to next line since filtering out drop table commands
                    continue;
                }

                // Filter out "drop sequence" oracle commands
                if (line.contains("drop sequence")) {
                    // Read in following blank line
                    line = reader.readLine();

                    // Continue to next line since filtering out drop commands
                    continue;
                }

                // Filter out the alter table commands where dropping a key or
                // a constraint
                if (line.contains("alter table")) {

                    if (line.contains("drop")) {

                        // Continue to next line since filtering out drop commands
                        continue;
                    }
                }

                // Line not being filtered so write it to the file
                writer.write(line);
                writer.write("\n");
            }
        } catch (IOException e) {
            logger.error("Could not trim cruft from file " + outputFilename + " . " + e.getMessage(), e);
        } finally {
            try {
                if (reader != null) reader.close();
                if (writer != null) writer.close();
            } catch (IOException ignored) {
            }
        }

        // Move the temp file to the original name
        try {
            Files.copy(
                    new File(tmpFileName).toPath(),
                    new File(outputFilename).toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            Files.delete(new File(tmpFileName).toPath());
        } catch (IOException e) {
            logger.error("Could not rename file " + tmpFileName + " to " + outputFilename, e);
        }
    }

    /**
     * Method that actually creates the file.
     *
     * @param dialect to use
     */
    private void generate(Dialect dialect) {
        logger.info("Generating schema for {}", dialect);
        Map<String, String> settings = new HashMap<>();
        settings.put("hibernate.dialect", dialect.getDialectClass());
        settings.put("hibernate.connection.driver_class", "org.h2.Driver");
        settings.put("hibernate.connection.url", "jdbc:h2:mem:testdb");
        settings.put("hibernate.connection.username", "sa");
        settings.put("hibernate.connection.password", "password");

        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(settings)
                .build();

        // Determine file name. Use "ddl_" plus dialect name such as mysql or
        // oracle plus the package name with "_" replacing "." such as
        // org_transitime_db_structs .
        String packeNameSuffix = packageName.replace(".", "_");
        String outputFilename = (outputDirectory != null ? outputDirectory + "/" : "") + "ddl_"
                + dialect.name().toLowerCase() + "_"
                + packeNameSuffix + ".sql";

        var metadataSources = new MetadataSources(serviceRegistry);

        for (Class<Object> annotatedClass : classList) {
            metadataSources.addAnnotatedClass(annotatedClass);
        }

        var metadata = metadataSources.buildMetadata();
        new SchemaExport()
                .setDelimiter(";") //
                .setOutputFile(outputFilename)
                .create(EnumSet.of(TargetType.SCRIPT), metadata);

        metadata.buildSessionFactory().close();

        // Get rid of unneeded SQL for dropping tables and keys and such
        trimCruftFromFile(outputFilename);
    }

    /**
     * Utility method used to fetch Class list based on a package name.
     *
     * @param packageName (should be the package containing your annotated beans.
     */
    @SuppressWarnings("rawtypes")
    private List<Class> getClasses(String packageName) throws Exception {

        List<Class> classes = new ArrayList<>();
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();

        for (final ClassPath.ClassInfo info : ClassPath.from(loader).getTopLevelClasses()) {
            if (info.getName().startsWith(packageName)) {
                final Class<?> clazz = info.load();
                classes.add(clazz);
            }
        }

        return classes;
    }

    /**
     * Holds the class names of hibernate dialects for easy reference.
     */
    @Getter
    private enum Dialect {
        // Note that using special ImprovedMySqlDialect
        MYSQL("org.transitclock.applications.SchemaGenerator$ImprovedMySQLDialect"),
        POSTGRES("org.hibernate.dialect.PostgreSQLDialect"),
        HSQL("org.hibernate.dialect.HSQLDialect");

        private final String dialectClass;

        Dialect(String dialectClass) {
            this.dialectClass = dialectClass;
        }
    }

    /**
     * Param args args[0] is the package name for the Hibernate annotated
     * classes whose schema is to be exported such as
     * "org.transitclock.db.structs". args[1] is optional output directory where
     * the resulting files are to go. If the optional output directory is not
     * specified then schema files written to local directory.
     * <p>
     * The resulting files have the name "ddl_" plus dialect name such as mysql
     * or oracle plus the first two components of the package name such as
     * org_transitclock.
     */
    public static void main(String[] args) throws Exception {
        // Handle the command line options
        CommandLineParser parser = new BasicParser();
        Options options = new Options();
        Option hibernatePackagePathOption = new Option(
                "p",
                "hibernatePackagePath",
                true,
                "This is the path to the package containing the " + "hibernate annotated java classes");

        Option outputDirectoryOption =
                new Option("o", "outputDirectory", true, "This is the directory to output the sql");
        hibernatePackagePathOption.setRequired(true);
        outputDirectoryOption.setRequired(true);
        options.addOption(outputDirectoryOption);
        options.addOption(hibernatePackagePathOption);

        try {
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("p") && cmd.hasOption("o")) {
                String packageName = cmd.getOptionValue("p");
                String outputDirectory = cmd.getOptionValue("o");

                // Note: need to use separate SchemaGenerator objects for each
                // dialect because for some reason they otherwise interfere
                // with each other.
                SchemaGenerator gen = new SchemaGenerator(packageName, outputDirectory);
                gen.generate(Dialect.POSTGRES);
                gen.generate(Dialect.HSQL);
                gen.generate(Dialect.MYSQL);
            } else {
                // Necessary command line options were not set
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("java -jar generateDatabaseSchema.jar", options);
                System.exit(-1);
            }
        } catch (ParseException pe) {
            logger.error(pe.getMessage(), pe);

            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar generateDatabaseSchema.jar", options);
            System.exit(-1);
        }
    }
}
