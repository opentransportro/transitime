package org.transitclock.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponseException;
import org.transitclock.api.exception.ProblemDetailWithCause.ProblemDetailWithCauseBuilder;

import java.net.URI;

@SuppressWarnings("java:S110") // Inheritance tree of classes should not be too deep
public class BadRequestException extends ErrorResponseException {

    public BadRequestException(String message) {
        super(
            HttpStatus.BAD_REQUEST,
            ProblemDetailWithCause.ProblemDetailWithCauseBuilder
                .instance()
                .withStatus(HttpStatus.BAD_REQUEST.value())
                .withDetail(message)
                .build(),
            null
        );
    }
}
