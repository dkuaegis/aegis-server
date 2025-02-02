package aegis.server.domain.survey.dto;

import aegis.server.domain.survey.domain.Interest;
import aegis.server.domain.survey.domain.Survey;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Map;
import java.util.Set;

public record SurveyCommon(
        @NotEmpty
        Set<Interest> interests,
        @Nullable
        Map<Interest, @NotBlank String> interestsEtc,
        @Size(min = 5, max = 1000)
        String joinReason,
        @Size(max = 1000)
        String feedback
) {
    public static SurveyCommon from(Survey survey) {
        return new SurveyCommon(
                survey.getInterests(),
                survey.getInterestsEtc(),
                survey.getJoinReason(),
                survey.getFeedback()
        );
    }
}
