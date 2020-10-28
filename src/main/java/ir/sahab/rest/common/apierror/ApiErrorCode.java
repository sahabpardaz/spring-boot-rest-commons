package ir.sahab.rest.common.apierror;

import ir.sahab.rest.common.requestlog.EnableCustomRequestLogging;
import org.zalando.problem.Status;

/**
 * Common interface for error codes that can be thrown from REST APIs. When a method inside a REST controller, throws an
 * {@link ApiException}, the {@link ApiErrorCode} inside the exception is used to make the HTTP response. In fact, the
 * status code indicates the HTTP status code of the response and other fields are written in the response body as a
 * JSON object. The mapping from {@link ApiException} to the corresponding HTTP response is done automatically if you
 * enable it using {@link EnableCustomRequestLogging} in your Spring boot application.
 */
public interface ApiErrorCode {

    String getName();

    Status getHttpStatusCode();

    String getEnMessage();

    String getFaMessage();
}
