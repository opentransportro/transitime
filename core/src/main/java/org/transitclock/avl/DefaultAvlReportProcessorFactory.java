package org.transitclock.avl;

import org.transitclock.db.structs.AvlReport;

public class DefaultAvlReportProcessorFactory implements AvlReportProcessorFactory {
    @Override
    public AvlReportProcessor createClient(AvlReport report) {
        return new AvlReportProcessor(report);
    }
}
