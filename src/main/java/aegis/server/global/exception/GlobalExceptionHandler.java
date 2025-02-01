package aegis.server.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
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

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.error("DataIntegrityViolationException : {}", e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(ErrorCode.ALREADY_EXISTS));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("INTERNAL_SERVER_ERROR : {}", e.getMessage(), e);
        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR));
    }

    // @Valid 어노테이션을 통한 검증에 실패할 시 실행됨
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

    // JSON 형식 오류 또는 Enum 변환에 실패할 시 실행됨
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException e, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.error("HTTP_MESSAGE_NOT_READABLE : {}", e.getMessage(), e);

        Throwable cause = e.getCause();

        // 1. JSON 형식 오류
        if (cause instanceof com.fasterxml.jackson.core.JsonParseException || cause instanceof com.fasterxml.jackson.databind.exc.MismatchedInputException) {
            return ResponseEntity
                    .status(ErrorCode.INVALID_JSON.getHttpStatus())
                    .body(ErrorResponse.of(ErrorCode.INVALID_JSON));
        }

        // 2. Enum 변환 오류
        if (cause instanceof IllegalArgumentException) {
            return ResponseEntity
                    .status(ErrorCode.INVALID_ENUM.getHttpStatus())
                    .body(ErrorResponse.of(ErrorCode.INVALID_ENUM));
        }

        // 3. 그 외의 메시지 읽기 실패 오류
        return ResponseEntity
                .status(ErrorCode.BAD_REQUEST.getHttpStatus())
                .body(ErrorResponse.of(ErrorCode.BAD_REQUEST));
    }
}
