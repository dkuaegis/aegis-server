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

    // Survey
    INVALID_INTEREST(HttpStatus.BAD_REQUEST),
    ETC_INTEREST_NOT_FOUND(HttpStatus.BAD_REQUEST),
    SURVEY_NOT_FOUND(HttpStatus.NOT_FOUND),

    // Coupon
    COUPON_DISCOUNT_AMOUNT_NOT_POSITIVE(HttpStatus.BAD_REQUEST),
    COUPON_NOT_FOUND(HttpStatus.NOT_FOUND),
    COUPON_ALREADY_EXISTS(HttpStatus.CONFLICT),
    COUPON_ALREADY_USED(HttpStatus.CONFLICT),

    ISSUED_COUPON_NOT_FOUND_FOR_MEMBER(HttpStatus.NOT_FOUND),

    // Payment
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND),
    PAYMENT_ALREADY_COMPLETED(HttpStatus.CONFLICT),
    PAYMENT_ALREADY_OVER_PAID(HttpStatus.CONFLICT),

    // Discord
    DISCORD_CANNOT_ISSUE_VERIFICATION_CODE(HttpStatus.INTERNAL_SERVER_ERROR),
    DISCORD_GUILD_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR),
    DISCORD_CHANNEL_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR),
    DISCORD_ROLE_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR),

    ;
    private final HttpStatus httpStatus;
}
