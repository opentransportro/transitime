/* (C)2023 */
package org.transitclock.api.reports;

import lombok.extern.slf4j.Slf4j;
import org.transitclock.api.reports.ChartJsonBuilder.RowBuilder;
import org.transitclock.domain.GenericQuery;

import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

/**
 * For providing data to a Google scatter chart when need to specify specific SQL for retrieving
 * data from the database. Since any SQL statement can be used this feed can be used for generating
 * a variety of scatter charts.
 *
 * @author SkiBu Smith
 */
@Slf4j
public class ChartGenericJsonQuery extends GenericQuery {
    private final ChartJsonBuilder jsonBuilder;


    /**
     * @param agencyId
     * @throws SQLException
     */
    public ChartGenericJsonQuery(String agencyId) throws SQLException {
        super(agencyId);
        jsonBuilder = new ChartJsonBuilder();
    }

    /* (non-Javadoc)
     * @see org.transitclock.db.GenericQuery#addColumn(java.lang.String, int)
     */
    @Override
    protected void addColumn(String columnName, int type) {
        if (columnName.equals("tooltip")) jsonBuilder.addTooltipColumn();
        else if (type == Types.NUMERIC
                || type == Types.INTEGER
                || type == Types.SMALLINT
                || type == Types.BIGINT
                || type == Types.DECIMAL
                || type == Types.FLOAT
                || type == Types.DOUBLE) jsonBuilder.addNumberColumn(columnName);
        else if (type == Types.VARCHAR) jsonBuilder.addStringColumn(columnName);
        else logger.error("Unknown type={} for columnName={}", type, columnName);
    }

    /* (non-Javadoc)
     * @see org.transitclock.db.GenericQuery#addRow(java.util.List)
     */
    @Override
    protected void addRow(List<Object> values) {
        // Start building up the row
        RowBuilder rowBuilder = jsonBuilder.newRow();

        // Add each cell in the row
        for (Object o : values) {
            rowBuilder.addRowElement(o);
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
    public static String getJsonString(String agencyId, String sql, Object... parameters) throws SQLException {
        ChartGenericJsonQuery query = new ChartGenericJsonQuery(agencyId);

        logger.info("Running query {}", sql);
        query.doQuery(sql, parameters);
        // If query returns empty set then should return null!
        if (query.getNumberOfRows() != 0) {
            return query.jsonBuilder.getJson();
        }

        return null;
    }
}
