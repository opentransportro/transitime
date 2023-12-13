/* (C)2023 */
package org.transitclock.misc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Test {
    private static final Logger s_logger = LoggerFactory.getLogger(Test.class);

    public static void log() {
        s_logger.error("Test error message");
        s_logger.info("Test info message");
        s_logger.debug("Test debug message");
    }
}
