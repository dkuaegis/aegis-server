package aegis.server.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    BAD_REQUEST(HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR),

    INVALID_ENUM(HttpStatus.BAD_REQUEST),
    INVALID_JSON(HttpStatus.BAD_REQUEST),

    ALREADY_EXISTS(HttpStatus.CONFLICT),

    // Member & Student
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND),
    STUDENT_NOT_FOUND(HttpStatus.NOT_FOUND),

    ;
    private final HttpStatus httpStatus;
}
