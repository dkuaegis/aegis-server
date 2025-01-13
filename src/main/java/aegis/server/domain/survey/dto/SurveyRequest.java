package aegis.server.domain.survey.dto;

import aegis.server.domain.survey.domain.InterestField;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;
import java.util.Set;

@Getter
@Builder
@AllArgsConstructor
public class SurveyRequest {

    private Set<InterestField> interestFields;

    @Nullable
    private Map<InterestField, String> interestEtc;

    @Size(min = 3, max = 1000)
    private String registrationReason;

    @Size(max = 1000)
    private String feedBack;

}
