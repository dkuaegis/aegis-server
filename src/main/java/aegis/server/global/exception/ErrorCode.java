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

    NOT_DKU_EMAIL(HttpStatus.BAD_REQUEST),

    // Member & Student
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND),
    STUDENT_NOT_FOUND(HttpStatus.NOT_FOUND),

    // Survey
    INVALID_INTEREST(HttpStatus.BAD_REQUEST),
    ETC_INTEREST_NOT_FOUND(HttpStatus.BAD_REQUEST),
    SURVEY_NOT_FOUND(HttpStatus.NOT_FOUND),

    // Discord
    DISCORD_CANNOT_ISSUE_VERIFICATION_CODE(HttpStatus.INTERNAL_SERVER_ERROR),
    DISCORD_GUILD_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR),
    DISCORD_CHANNEL_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR),
    DISCORD_ROLE_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR),

    // Timetable
    TIMETABLE_INVALID_URL(HttpStatus.BAD_REQUEST),
    TIMETABLE_FETCH_EVERYTIME_NOT_WORKING(HttpStatus.GATEWAY_TIMEOUT),
    TIMETABLE_PARSE_NOT_FOUND(HttpStatus.NOT_FOUND), // 시간표를 찾을 수 없는 경우
    TIMETABLE_PARSE_PRIVATE(HttpStatus.FORBIDDEN), // 시간표 미공개 또는 등록된 강의가 없는 경우
    TIMETABLE_PARSE_XML_FAILED(HttpStatus.INTERNAL_SERVER_ERROR),
    TIMETABLE_PARSE_JSON_FAILED(HttpStatus.INTERNAL_SERVER_ERROR),

    TIMETABLE_IDENTIFIER_ALREADY_EXISTS(HttpStatus.CONFLICT),

    // Coupon
    COUPON_DISCOUNT_AMOUNT_NOT_POSITIVE(HttpStatus.BAD_REQUEST),
    COUPON_NOT_FOUND(HttpStatus.NOT_FOUND),
    COUPON_ALREADY_EXISTS(HttpStatus.CONFLICT),
    COUPON_ALREADY_USED(HttpStatus.CONFLICT),
    COUPON_ISSUED_COUPON_EXISTS(HttpStatus.CONFLICT), // 발급된 쿠폰이 존재하는 경우 발급된 쿠폰을 모두 지워야 삭제 가능

    ISSUED_COUPON_NOT_FOUND(HttpStatus.NOT_FOUND),
    ISSUED_COUPON_NOT_FOUND_FOR_MEMBER(HttpStatus.BAD_REQUEST),

    COUPON_CODE_NOT_FOUND(HttpStatus.NOT_FOUND),
    COUPON_CODE_ALREADY_USED(HttpStatus.CONFLICT),
    COUPON_CODE_CANNOT_ISSUE_CODE(HttpStatus.INTERNAL_SERVER_ERROR),

    // Payment
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND),
    PAYMENT_ALREADY_COMPLETED(HttpStatus.CONFLICT),
    PAYMENT_ALREADY_OVER_PAID(HttpStatus.CONFLICT),
    PAYMENT_CANNOT_BE_CONFIRMED(HttpStatus.INTERNAL_SERVER_ERROR),


    ;
    private final HttpStatus httpStatus;
}
