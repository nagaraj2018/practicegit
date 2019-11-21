package loancalc.apps.exception;

public class LoanAppApiExceptionResponse {

    private String errorMessage;

    LoanAppApiExceptionResponse(final String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(final String errorMessage) {
        this.errorMessage = errorMessage;
    }
}