package org.transitclock.core.avl;

import org.transitclock.domain.structs.AvlReport;

public class DefaultAvlReportProcessorFactory implements AvlReportProcessorFactory {
    @Override
    public AvlReportProcessor createClient(AvlReport report) {
        return new AvlReportProcessor(report);
    }
}
