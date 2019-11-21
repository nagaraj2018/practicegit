package loancalc.apps.exception;

import org.springframework.http.HttpStatus;

public class LoanAppApiException extends RuntimeException {

    private final String errorMessage;
    private final HttpStatus httpStatus;

    public LoanAppApiException(final String errorMessage, final HttpStatus httpStatus) {
        this.errorMessage = errorMessage;
        this.httpStatus = httpStatus;
    }

    String getErrorMessage() {
        return errorMessage;
    }

    HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
