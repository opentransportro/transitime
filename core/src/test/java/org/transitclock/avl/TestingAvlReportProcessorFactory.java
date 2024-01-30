package org.transitclock.avl;

import lombok.extern.slf4j.Slf4j;
import org.transitclock.domain.structs.AvlReport;
import org.transitclock.utils.Time;

public class TestingAvlReportProcessorFactory implements AvlReportProcessorFactory {
    /**
     * Separate executor, just for testing. The run method simply sleeps for a while so can verify
     * that the queuing works when system getting behind in the processing of AVL reports
     */
    @Slf4j
    private static class AvlReportProcessorTester extends AvlReportProcessor {
        private AvlReportProcessorTester(AvlReport avlReport) {
            super(avlReport);
        }

        /** Delays for a while */
        @Override
        public void run() {
            // Let each call get backed up so can see what happens to queue
            logger.info("Starting processing of {}", getAvlReport());
            Time.sleep(6 * Time.SEC_IN_MSECS);
            logger.info("Finished processing of {}", getAvlReport());
        }
    }

    @Override
    public AvlReportProcessor createClient(AvlReport report) {
        return new AvlReportProcessorTester(report);
    }
}
