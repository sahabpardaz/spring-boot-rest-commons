package ir.sahab.rest.common.apierror;

import java.util.UUID;

/**
 * This is the only one exception which is used for throwing business exception through any REST API.
 *
 * @see ApiExceptionHandler
 */
public class ApiException extends Exception {
    private static final long serialVersionUID = 1L;

    private final String trackingId = UUID.randomUUID().toString();
    private final ApiErrorCode error;
    private final Object extraData;

    public ApiException(ApiErrorCode error) {
        this(error, null, null);
    }

    public ApiException(ApiErrorCode error, Throwable throwable) {
        this(error, null, throwable);
    }

    public ApiException(ApiErrorCode error, Object extraData) {
        this(error, extraData, null);
    }

    public ApiException(ApiErrorCode error, Object extraData, Throwable throwable) {
        super(error.getEnMessage(), throwable);
        this.extraData = extraData;
        this.error = error;
    }

    /**
     * Tracking ID is the unique ID generated for each API exception and it is useful to track an error and find more
     * about it. Note that some part of the system such as UI front-end modules, does not access to full description of
     * an error (mostly for security reasons) but they get the track ID anyway. Then this track ID can be used to get
     * further information about the error from different sources like logs in ELK or error records in Sentry.
     */
    String getTrackingId() {
        return trackingId;
    }

    /**
     * Error code is a standard code defined for an API error.
     */
    public ApiErrorCode getApiErrorCode() {
        return error;
    }

    /**
     * Extra data is descriptive fields containing further information about the error code. Some error codes require no
     * extra data but some others do.
     */
    public Object getExtraData() {
        return extraData;
    }
}
