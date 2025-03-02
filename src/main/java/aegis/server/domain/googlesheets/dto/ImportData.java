package aegis.server.domain.googlesheets.dto;

import aegis.server.domain.member.domain.*;
import aegis.server.domain.survey.domain.AcquisitionType;
import aegis.server.domain.survey.domain.Interest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record ImportData(
        LocalDateTime joinDateTime,
        String name,
        String studentId,
        Department department,
        Grade grade,
        Semester semester,
        AcademicStatus academicStatus,
        String phoneNumber,
        String discordId,
        String email,
        String birthDate,
        Gender gender,
        Boolean fresh,
        Set<Interest> interests,
        AcquisitionType acquisitionType,
        String joinReason,
        String feedback,
        BigDecimal finalPrice
) {
    public List<Object> toRowData() {
        return List.of(
                joinDateTime != null ? joinDateTime.toString() : "",
                name,
                studentId,
                department != null ? department.getValue() : "",
                grade != null ? grade.getValue() : "",
                semester != null ? semester.getValue() : "",
                academicStatus != null ? academicStatus.getValue() : "",
                phoneNumber,
                discordId,
                email,
                birthDate,
                gender != null ? gender.getValue() : "",
                fresh != null ? (fresh ? "신규" : "재등록") : "NULL",
                interests != null ? interests.stream()
                        .map(Interest::getValue).collect(Collectors.joining(",")) : "",
                acquisitionType != null ? acquisitionType.toString() : "NULL",
                joinReason,
                feedback != null && !feedback.isEmpty() ? feedback : "NULL",
                finalPrice != null ? finalPrice.toString() : ""
        );
    }
}
