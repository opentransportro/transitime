/* (C)2023 */
package org.transitclock.custom.aws;

import com.amazonaws.services.sqs.model.Message;
import java.util.List;

/** Interface for deserializing an AWS SQS Message into an AVLReport. */
public interface SqsMessageUnmarshaller {

    AvlReportWrapper toAvlReport(Message message) throws Exception;

    String toString(Message message) throws Exception;

    List<AvlReportWrapper> toAvlReports(Message message);
}
