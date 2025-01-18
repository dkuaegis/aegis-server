package aegis.server.domain.member.dto.request;

import aegis.server.domain.member.domain.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberUpdateRequest {

    @NotBlank
    @Pattern(regexp = "^[0-9]{2}(0[1-9]|1[0-2])(0[1-9]|[1-2][0-9]|3[0-1])$",
            message = "생일은 YYMMDD 형식이어야 합니다.")
    private String birthDate;

    @NotNull
    private Gender gender;

    @NotBlank
    @Pattern(regexp = "^32\\d{6}$", message = "학번은 '32'로 시작하는 8자리 숫자여야 합니다.")
    private String studentId;

    @NotBlank
    @Pattern(regexp = "^010-[0-9]{4}-[0-9]{4}$", message = "전화번호는 '010-XXXX-YYYY' 형식이어야 합니다")
    private String phoneNumber;

    @NotNull
    private Department department;

    @NotNull
    private AcademicStatus academicStatus;

    @NotNull
    private Grade grade;

    @NotNull
    private Semester semester;

}