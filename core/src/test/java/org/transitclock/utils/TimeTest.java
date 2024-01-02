package org.transitclock.utils;

import java.text.ParseException;
import java.util.Date;

import org.junit.jupiter.api.Test;

import static org.transitclock.utils.Time.SEC_PER_HOUR;
import static org.transitclock.utils.Time.parse;

class TimeTest {
    @Test
    void testParsing() {
        try {
            // TODO make this a unit test
            Time time = new Time("Europe/Bucharest");
            Date referenceDate = parse("23-11-2013 23:55:00");
            int secondsIntoDay = 24 * SEC_PER_HOUR - 60;
            long epochTime = time.getEpochTime(secondsIntoDay, referenceDate);
            System.out.println(new Date(epochTime));
        } catch (ParseException e) {
        }
    }
}