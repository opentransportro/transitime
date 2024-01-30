/* (C)2023 */
package org.transitclock.api.reports;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.domain.GenericQuery;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class GenericJsonQuery extends GenericQuery {

    private static final Logger logger = LoggerFactory.getLogger(GenericJsonQuery.class);
    private StringBuilder strBuilder = new StringBuilder();
    private List<String> columnNames = new ArrayList<String>();
    private boolean firstRow = true;

    /**
     * @param agencyId
     * @throws SQLException
     */
    private GenericJsonQuery(String agencyId) throws SQLException {
        super(agencyId);
    }

    /* (non-Javadoc)
     * @see org.transitclock.db.GenericQuery#addColumn(java.lang.String, int)
     */
    @Override
    protected void addColumn(String columnName, int type) {
        // Keep track of names of all columns
        columnNames.add(columnName);
    }

    private void addRowElement(int i, double value) {
        strBuilder.append(value);
    }

    private void addRowElement(int i, long value) {
        strBuilder.append(value);
    }

    private void addRowElement(int i, String value) {
        strBuilder.append("\"").append(value).append("\"");
    }

    private void addRowElement(int i, Timestamp value) {
        strBuilder.append("\"").append(value).append("\"");
    }

    /* (non-Javadoc)
     * @see org.transitclock.db.GenericQuery#addRow(java.util.List)
     */
    @Override
    protected void addRow(List<Object> values) {
        if (!firstRow) strBuilder.append(",\n");
        firstRow = false;

        strBuilder.append('{');

        // Add each cell in the row
        boolean firstElementInRow = true;
        for (int i = 0; i < values.size(); ++i) {
            Object o = values.get(i);
            // Can't output null attributes
            if (o == null) continue;

            if (!firstElementInRow) strBuilder.append(",");
            firstElementInRow = false;

            // Output name of attribute
            strBuilder.append("\"").append(columnNames.get(i)).append("\":");

            // Output value of attribute
            if (o instanceof Double || o instanceof Float) {
                addRowElement(i, ((Number) o).doubleValue());
            } else if (o instanceof Number) {
                addRowElement(i, ((Number) o).longValue());
            } else if (o instanceof String) {
                addRowElement(i, (String) o);
            } else if (o instanceof Timestamp) {
                addRowElement(i, ((Timestamp) o));
            }
        }

        strBuilder.append('}');
    }

    /**
     * Does SQL query and returns JSON formatted results.
     *
     * @param agencyId
     * @param sql
     * @return
     * @throws SQLException
     */
    public static String getJsonString(String agencyId, String sql, Object... parameters) {
        // Add the rows from the query to the JSON string
        try {
            GenericJsonQuery query = new GenericJsonQuery(agencyId);

            // Start the JSON
            query.strBuilder.append("{\"data\": [\n");

            query.doQuery(sql, parameters);

            // Finish up the JSON
            query.strBuilder.append("]}");

            return query.strBuilder.toString();
        } catch (SQLException e) {
            return e.getMessage();
        }
    }

    /**
     * Does SQL query and returns JSON formatted results.
     *
     * @param agencyId
     * @param sql
     * @return
     * @throws SQLException
     */
    public static String getJsonString(String agencyId, String sql) {
        // Add the rows from the query to the JSON string
        try {
            GenericJsonQuery query = new GenericJsonQuery(agencyId);

            // Start the JSON
            query.strBuilder.append("{\"data\": [\n");
            logger.debug("sql=" + sql);

            query.doQuery(sql);

            // Finish up the JSON
            query.strBuilder.append("]}");

            return query.strBuilder.toString();
        } catch (SQLException e) {
            return e.getMessage();
        }
    }

    public static void main(String[] args) {
        String agencyId = "sfmta";

        String sql = "SELECT * FROM avlreports ORDER BY time DESC LIMIT 5";
        String str = GenericJsonQuery.getJsonString(agencyId, sql);
        System.out.println(str);
    }
}
