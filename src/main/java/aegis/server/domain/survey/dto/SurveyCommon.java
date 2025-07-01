package aegis.server.domain.survey.dto;

import java.util.Map;
import java.util.Set;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import aegis.server.domain.survey.domain.AcquisitionType;
import aegis.server.domain.survey.domain.Interest;
import aegis.server.domain.survey.domain.Survey;

public record SurveyCommon(
        @NotEmpty Set<Interest> interests,
        @Nullable Map<Interest, @NotBlank String> interestsEtc,
        @NotNull AcquisitionType acquisitionType,
        @Size(min = 5, max = 1000) String joinReason,
        @Size(max = 1000) String feedback) {
    public static SurveyCommon from(Survey survey) {
        return new SurveyCommon(
                survey.getInterests(),
                survey.getInterestsEtc(),
                survey.getAcquisitionType(),
                survey.getJoinReason(),
                survey.getFeedback());
    }
}
