package aegis.server.domain.study.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AttendanceMarkRequest(
        @NotBlank @Pattern(regexp = "^[0-9]{4}$", message = "코드는 4자리 숫자여야 합니다") String code) {

    public static AttendanceMarkRequest of(String code) {
        return new AttendanceMarkRequest(code);
    }
}
