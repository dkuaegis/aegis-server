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
    ;

    private final HttpStatus httpStatus;
}
