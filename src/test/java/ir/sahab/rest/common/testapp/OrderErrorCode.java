package ir.sahab.rest.common.testapp;

import ir.sahab.rest.common.apierror.ApiErrorCode;
import org.zalando.problem.Status;

/**
 * Business error codes that can be thrown from Order REST APIs.
 */
public enum OrderErrorCode implements ApiErrorCode {

    // Common error codes
    NOT_AVAILABLE_IN_STORE(Status.BAD_REQUEST,
            "There is not enough quantity of the requested product in the store: product = %s",
            "به مقدار کافی از محصول مورد نظر در انبار موجود نیست: محصول = %s");

    private final Status status;
    private final String enMessage;
    private final String faMessage;

    OrderErrorCode(Status status, String enMessage, String faMessage) {
        this.status = status;
        this.enMessage = enMessage;
        this.faMessage = faMessage;
    }

    @Override
    public String getName() {
        return name();
    }

    @Override
    public Status getHttpStatusCode() {
        return status;
    }

    @Override
    public String getEnMessage() {
        return enMessage;
    }

    @Override
    public String getFaMessage() {
        return faMessage;
    }
}
