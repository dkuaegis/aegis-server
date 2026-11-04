package aegis.server.domain.survey.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;

import aegis.server.domain.survey.domain.AcquisitionType;
import aegis.server.domain.survey.domain.Survey;

public record SurveyCommon(
        @NotNull AcquisitionType acquisitionType,
        @Size(min = 5, max = 1000) @Schema(nullable = true) String joinReason) {
    public static SurveyCommon from(Survey survey) {
        return new SurveyCommon(survey.getAcquisitionType(), survey.getJoinReason());
    }
}
