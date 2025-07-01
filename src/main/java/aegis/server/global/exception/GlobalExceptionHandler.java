package aegis.server.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Value("${exception.log-only-aegis-stack-trace}")
    private boolean logOnlyAegisStackTrace;

    private static final String[] EXCLUDED_CLASSES = {
            "aegis.server.global.security.oidc.RefererFilter"
    };

    /**
     * 예외 발생 시 스택 트레이스 중 {@link GlobalExceptionHandler#logOnlyAegisStackTrace} 값이 true면 aegis.server 패키지의 요소만 필터링하여 로깅합니다.
     */
    private void logFilteredException(Exception e, String errorType) {
        StringBuilder sb = new StringBuilder();
        sb.append(errorType).append(": ").append(e.getMessage());

        List<StackTraceElement> stackTraceElements;
        if (logOnlyAegisStackTrace) {
            stackTraceElements = Arrays.stream(e.getStackTrace())
                    .filter(element -> element.getClassName().startsWith("aegis.server"))
                    .filter(element -> Arrays.stream(EXCLUDED_CLASSES)
                            .noneMatch(excluded -> excluded.equals(element.getClassName())))
                    .toList();
        } else {
            stackTraceElements = Arrays.asList(e.getStackTrace());
        }

        for (StackTraceElement element : stackTraceElements) {
            sb.append("\n")
                    .append("\tat ")
                    .append(element.getClassName())
                    .append(".")
                    .append(element.getMethodName())
                    .append("(")
                    .append(element.getFileName())
                    .append(":")
                    .append(element.getLineNumber())
                    .append(")");
        }

        // 예외 타입에 따라 로깅 레벨을 다르게 설정
        // WARN:  500 미만의 HTTP 상태 코드를 가지는 CustomException과 DataIntegrityViolationException
        // ERROR: 500 이상의 HTTP 상태 코드를 가지는 CustomException과 일반 Exception
        if (e instanceof CustomException customException) {
            if (customException.getErrorCode().getHttpStatus().value() >= 500) {
                log.error(sb.toString());
            } else {
                log.warn(sb.toString());
            }
        } else if (e instanceof DataIntegrityViolationException) {
            log.warn(sb.toString());
        } else {
            log.error(sb.toString());
        }
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        logFilteredException(e, "CustomException");
        return ResponseEntity
                .status(e.getErrorCode().getHttpStatus())
                .body(ErrorResponse.of(e.getErrorCode()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        logFilteredException(e, "DataIntegrityViolationException");
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(ErrorCode.ALREADY_EXISTS));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        logFilteredException(e, "INTERNAL_SERVER_ERROR");
        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR));
    }

    // @Valid 어노테이션을 통한 검증에 실패할 시 실행됨
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        logFilteredException(e, "METHOD_ARGUMENT_NOT_VALID");

        List<FieldErrorDetail> fieldErrorDetails = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> FieldErrorDetail.of(
                        fieldError.getField(),
                        fieldError.getRejectedValue(),
                        fieldError.getDefaultMessage()
                ))
                .collect(Collectors.toList());

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
        logFilteredException(e, "HTTP_MESSAGE_NOT_READABLE");

        Throwable cause = e.getCause();

        // 1. JSON 형식 오류
        if (cause instanceof com.fasterxml.jackson.core.JsonParseException ||
                cause instanceof com.fasterxml.jackson.databind.exc.MismatchedInputException) {
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
