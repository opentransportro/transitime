package org.transitclock.core.avl;

import org.springframework.stereotype.Component;
import org.transitclock.core.AvlProcessor;
import org.transitclock.domain.structs.AvlReport;

@Component
public class DefaultAvlReportProcessorFactory implements AvlReportProcessorFactory {
    private final AvlProcessor avlProcessor;

    public DefaultAvlReportProcessorFactory(AvlProcessor avlProcessor) {
        this.avlProcessor = avlProcessor;
    }

    @Override
    public AvlReportProcessor createClient(AvlReport report) {
        return new AvlReportProcessor(report, avlProcessor);
    }
}
