package aegis.server.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR),
    METHOD_ARGUMENT_NOT_VALID(HttpStatus.BAD_REQUEST);

    private final HttpStatus httpStatus;
}
