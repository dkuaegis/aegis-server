package aegis.server.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        log.error("CustomException : {}", e.getMessage(), e);
        return ResponseEntity
                .status(e.getErrorCode().getHttpStatus())
                .body(ErrorResponse.of(e.getErrorCode()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("INTERNAL_SERVER_ERROR : {}", e.getMessage(), e);
        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.error("METHOD_ARGUMENT_NOT_VALID : {}", e.getMessage(), e);

        List<FieldErrorDetail> fieldErrorDetails = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> FieldErrorDetail.of(
                        fieldError.getField(),
                        fieldError.getRejectedValue(),
                        fieldError.getDefaultMessage()
                ))
                .toList();

        ArgumentNotValidErrorResponse errorResponse = ArgumentNotValidErrorResponse.of(
                ErrorCode.BAD_REQUEST,
                fieldErrorDetails
        );

        return ResponseEntity
                .status(ErrorCode.BAD_REQUEST.getHttpStatus())
                .body(errorResponse);
    }
}
