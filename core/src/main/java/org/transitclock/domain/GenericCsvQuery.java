/* (C)2023 */
package org.transitclock.domain;

import org.apache.commons.text.StringEscapeUtils;

import java.sql.SQLException;
import java.util.List;

/**
 * For doing an SQL query and returning the results in CVS format.
 *
 * @author SkiBu Smith
 */
public class GenericCsvQuery extends GenericQuery {

    // For putting in the CSV data
    private final StringBuilder sb = new StringBuilder();

    // So can determine if first column
    private int numColumns = 0;

    /**
     * Constructor
     *
     * @param agencyId
     * @throws SQLException
     */
    private GenericCsvQuery(String agencyId) throws SQLException {
        super(agencyId);
    }

    /* (non-Javadoc)
     * @see org.transitclock.db.GenericQuery#addColumn(java.lang.String, int)
     */
    @Override
    protected void addColumn(String columnName, int type) {
        if (numColumns++ > 0) {
            sb.append(',');
        }
        sb.append(columnName);
    }

    /* (non-Javadoc)
     * @see org.transitclock.db.GenericQuery#doneWithColumns()
     */
    @Override
    protected void doneWithColumns() {
        sb.append('\n');
    }

    /* (non-Javadoc)
     * @see org.transitclock.db.GenericQuery#addRow(java.util.List)
     */
    @Override
    protected void addRow(List<Object> values) {
        int column = 0;
        for (Object o : values) {
            // Comma separate the cells
            if (column++ > 0) {
                sb.append(',');
            }

            // Output value as long as it is not null
            if (o != null) {
                // Strings should be escaped but numbers can be output directly
                if (o instanceof String) {
                    sb.append(StringEscapeUtils.escapeCsv((String) o));
                } else {
                    sb.append(o);
                }
            }
        }
        sb.append('\n');
    }

    /**
     * Runs a query and returns result as a CSV string.
     *
     * @param agencyId For determining which database to access
     * @param sql The SQL to execute
     * @return CVS string of results from query
     * @throws SQLException
     */
    public static String getCsvString(String agencyId, String sql, Object... parameters) throws SQLException {
        GenericCsvQuery query = new GenericCsvQuery(agencyId);
        query.doQuery(sql, parameters);
        return query.sb.toString();
    }
}
