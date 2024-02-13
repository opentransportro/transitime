/* (C)2023 */
package org.transitclock.domain;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.transitclock.domain.hibernate.HibernateUtils;
import org.transitclock.utils.IntervalTimer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * For doing a query without using Hibernate. By using regular JDBC and avoiding Hibernate can
 * connect to multiple databases of different types.
 *
 * @author SkiBu Smith
 */
@Slf4j
public class GenericQuery {

    @Getter
    private final Connection connection;
    // Number of rows read in
    private int rows;

    /**
     * Constructor
     *
     * @param agencyId
     * @throws SQLException
     */
    public GenericQuery(String agencyId) throws SQLException {
        // Get the web agency. If it is really old, older than an hour then
        // update the cache in case the db was moved.

        connection = HibernateUtils.getSessionFactory(agencyId)
                .getSessionFactoryOptions()
                .getServiceRegistry()
                .getService(ConnectionProvider.class)
                .getConnection();
    }


    /**
     * Performs the specified generic query. A List of GenericResult objects is returned. All number
     * columns (integer or float) are placed in GenericResult.numbers. A string column is assumed to
     * be a tooltip and is put in GenericResult.text.
     *
     * @param sql
     * @return List of GenericResult. If no data then returns empty list (instead of null)
     * @throws SQLException
     */
    protected void doQuery(String sql, Object... parameters) throws SQLException {

        IntervalTimer timer = new IntervalTimer();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            // TODO Deal with dates for the moment
            for (int i = 0; i < parameters.length; i++) {
                if (parameters[i] instanceof java.util.Date) {
                    statement.setTimestamp(i + 1, new Timestamp(((java.util.Date) parameters[i]).getTime()));
                }
            }

            ResultSet rs = statement.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();

            // Add all the columns by calling subclass addColumn()
            for (int i = 1; i <= metaData.getColumnCount(); ++i) {
                addColumn(metaData.getColumnLabel(i), metaData.getColumnType(i));
            }
            doneWithColumns();

            // Process each row of data
            rows = 0;
            while (rs.next()) {
                ++rows;

                List<Object> row = new ArrayList<Object>();
                for (int i = 1; i <= metaData.getColumnCount(); ++i) {
                    row.add(rs.getObject(i));
                }
                addRow(row);
            }

            rs.close();

            logger.debug("GenericQuery query took {}msec rows={}", timer.elapsedMsec(), rows);
        } catch (SQLException e) {
            throw e;
        }
    }

    /**
     * Executes an INSERT, UPDATE, or DELETE statement.
     *
     * @param sql The SQL to be executed
     * @throws SQLException
     */
    public void doUpdate(String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            throw e;
        }
    }

    /**
     * Returns number of rows read in.
     *
     * @return
     */
    protected int getNumberOfRows() {
        return rows;
    }

    /**
     * Called for each column when processing query data
     *
     * @param columnName
     * @param type java.sql.Types such as Types.DOUBLE
     */
    protected void addColumn(String columnName, int type) {}

    /**
     * When done processing columns. Allows subclass to insert separator between column definitions
     * and the row data
     */
    protected void doneWithColumns() {}

    /**
     * Called for each row when processing query data.
     *
     * @param values The values for the row.
     */
    protected void addRow(List<Object> values) {}
}
