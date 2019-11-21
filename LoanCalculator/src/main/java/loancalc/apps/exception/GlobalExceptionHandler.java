package loancalc.apps.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({ LoanAppApiException.class })
    public ResponseEntity<Object> handleApiException(final LoanAppApiException apiException, final WebRequest webRequest) {
        logger.error("API Exception: {}", apiException);
        final LoanAppApiExceptionResponse apiExceptionResponse = new LoanAppApiExceptionResponse(apiException.getErrorMessage());
        return handleExceptionInternal(apiException, apiExceptionResponse, new HttpHeaders(),
                apiException.getHttpStatus(), webRequest);
    }
}
