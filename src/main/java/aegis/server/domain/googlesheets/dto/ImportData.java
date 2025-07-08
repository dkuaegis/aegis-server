package aegis.server.domain.googlesheets.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
        String discordId,
        String email,
        String birthDate,
        Gender gender,
        AcquisitionType acquisitionType,
        String joinReason,
        BigDecimal finalPrice) {
    public List<Object> toRowData() {
        String formattedDateTime = "";
        if (joinDateTime != null) {
            ZonedDateTime utcTime = joinDateTime.atZone(ZoneId.of("UTC"));
            ZonedDateTime koreaTime = utcTime.withZoneSameInstant(ZoneId.of("Asia/Seoul"));
            formattedDateTime = koreaTime.toLocalDateTime().toString();
        }

        return List.of(
                formattedDateTime,
                name,
                studentId,
                department != null ? department.getValue() : "",
                grade != null ? grade.getValue() : "",
                phoneNumber,
                discordId,
                email,
                birthDate,
                gender != null ? gender.getValue() : "",
                acquisitionType != null ? acquisitionType.getValue() : "NULL",
                joinReason,
                finalPrice != null ? finalPrice.toString() : "");
    }
}
