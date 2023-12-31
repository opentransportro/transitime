package org.transitclock.avl;

import org.transitclock.db.structs.AvlReport;

public interface AvlReportProcessorFactory {
    AvlReportProcessor createClient(AvlReport report);
}
