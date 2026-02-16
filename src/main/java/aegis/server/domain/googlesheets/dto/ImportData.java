package aegis.server.domain.googlesheets.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import aegis.server.domain.member.domain.*;
import aegis.server.domain.survey.domain.AcquisitionType;

public record ImportData(
        LocalDateTime joinDateTime,
        String name,
        String studentId,
        Department department,
        Grade grade,
        String phoneNumber,
        String email,
        String birthDate,
        Gender gender,
        AcquisitionType acquisitionType,
        String joinReason,
        BigDecimal finalPrice) {
    public List<Object> toRowData() {
        String formattedDateTime = "";
        if (joinDateTime != null) {
            formattedDateTime = joinDateTime.toString();
        }

        return List.of(
                formattedDateTime,
                name,
                studentId,
                department != null ? department.getValue() : "",
                grade != null ? grade.getValue() : "",
                phoneNumber,
                email,
                birthDate,
                gender != null ? gender.getValue() : "",
                acquisitionType != null ? acquisitionType.getValue() : "NULL",
                joinReason,
                finalPrice != null ? finalPrice.toString() : "");
    }
}
