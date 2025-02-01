package aegis.server.domain.member.dto.request;

import aegis.server.domain.member.domain.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record PersonalInfoUpdateRequest(
        @NotBlank
        @Pattern(regexp = "^[0-9]{2}(0[1-9]|1[0-2])(0[1-9]|[1-2][0-9]|3[0-1])$",
                message = "생일은 YYMMDD 형식이여야 합니다")
        String birthDate,
        @NotNull Gender gender,
        @NotBlank
        @Pattern(regexp = "^010-[0-9]{4}-[0-9]{4}$",
                message = "전화번호는 '010-1234-5678' 형식이어야 합니다")
        String phoneNumber,
        @NotBlank
        @Pattern(regexp = "^32\\d{6}$",
                message = "학번은 '32' 로 시작하는 8자리 숫자여야 합니다")
        String studentId,
        @NotNull Department department,
        @NotNull AcademicStatus academicStatus,
        @NotNull Grade grade,
        @NotNull Semester semester
) {
}
