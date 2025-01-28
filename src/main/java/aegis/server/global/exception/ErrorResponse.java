package aegis.server.global.exception;

public record ErrorResponse(String name) {

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.name());
    }
}
