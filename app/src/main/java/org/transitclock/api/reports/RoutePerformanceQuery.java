/* (C)2023 */
package org.transitclock.api.reports;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQuery;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.domain.hibernate.HibernateUtils;
import org.transitclock.domain.structs.PredictionAccuracy;
import org.transitclock.db.structs.QPredictionAccuracy;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * To find route performance information. For now, route performance is the percentage of
 * predictions for a route which are ontime.
 *
 * @author Simon Jacobs
 */
public class RoutePerformanceQuery {
    private Session session;

    private static final Logger logger = LoggerFactory.getLogger(RoutePerformanceQuery.class);

    public static final String PREDICTION_TYPE_AFFECTED = "AffectedByWaitStop";
    public static final String PREDICTION_TYPE_NOT_AFFECTED = "NotAffectedByWaitStop";

    private static final String TRANSITIME_PREDICTION_SOURCE = "Transitime";

    // TODO: revisit query
    public List<PredictionAccuracy> query(
            String agencyId,
            Date startDate,
            int numDays,
            double allowableEarlyMin,
            double allowableLateMin,
            String predictionType,
            String predictionSource) {

        int msecLo = (int) (allowableEarlyMin * 60 * 1000 * -1);
        int msecHi = (int) (allowableLateMin * 60 * 1000);
        Calendar c = Calendar.getInstance();
        c.setTime(startDate);
        c.add(Calendar.DAY_OF_YEAR, numDays);
        Date endDate = c.getTime();
        // Project to: # of predictions in which route is on time / # of predictions
        // for route. This cannot be done with pure Criteria API. This could be
        // moved to a separate class or XML file.
        try {
            session = HibernateUtils.getSession(agencyId);

            JPAQuery<PredictionAccuracy> query = new JPAQuery<>(session);
            var qentity = QPredictionAccuracy.predictionAccuracy;
            NumberPath<Double> performance = Expressions.numberPath(Double.class, "performance");
            query.from(qentity)
                    .select(qentity.predictionAccuracyMsecs.avg().as(performance))
                    .where(qentity.arrivalDepartureTime.between(startDate,endDate))
                    .where(qentity.predictionAccuracyMsecs.between(msecLo,msecHi));


            if (predictionType == PREDICTION_TYPE_AFFECTED)
                query.where(qentity.affectedByWaitStop.eq(true));
            else if (predictionType == PREDICTION_TYPE_NOT_AFFECTED)
                query.where(qentity.affectedByWaitStop.eq(false));

            if (predictionSource != null && !StringUtils.isEmpty(predictionSource)) {
                if (predictionSource.equals(TRANSITIME_PREDICTION_SOURCE))
                    query.where(qentity.predictionSource.eq(TRANSITIME_PREDICTION_SOURCE));
                else
                    query.where(qentity.predictionSource.ne(TRANSITIME_PREDICTION_SOURCE));
            }

            query.orderBy(performance.desc())
                    .groupBy(qentity.routeId);

            @SuppressWarnings("unchecked")
            List<PredictionAccuracy> results = query.fetch();

            return results;
        } catch (HibernateException e) {
            logger.error(e.toString());
            return null;
        } finally {
            session.close();
        }
    }
}
