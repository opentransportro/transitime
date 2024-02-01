package org.transitclock.core.avl;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.transitclock.domain.structs.AvlReport;

class AvlExecutorTest {
    @Test
    @Disabled
    public void processAvlReport() {
        AvlExecutor executor = new AvlExecutor(new TestingAvlReportProcessorFactory());

        executor.processAvlReport(new AvlReport("v1", 0, 12.34, 43.21, null));
        executor.processAvlReport(new AvlReport("v2", 1000, 12.34, 43.21, null));
        executor.processAvlReport(new AvlReport("v3", 2000, 12.34, 43.21, null));
        executor.processAvlReport(new AvlReport("v1", 3000, 12.34, 43.21, null));
        executor.processAvlReport(new AvlReport("v2", 4000, 12.34, 43.21, null));
        executor.processAvlReport(new AvlReport("v3", 5000, 12.34, 43.21, null));
        executor.processAvlReport(new AvlReport("v1", 6000, 12.34, 43.21, null));
        executor.processAvlReport(new AvlReport("v1", 7000, 12.34, 43.21, null));
        executor.processAvlReport(new AvlReport("v1", 8000, 12.34, 43.21, null));
    }
}