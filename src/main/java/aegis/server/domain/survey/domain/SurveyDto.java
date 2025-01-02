package aegis.server.domain.survey.domain;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
public class SurveyDto {

    private Long memberId;
    private Set<InterestField> interestFields = new HashSet<>();

    @Size(min = 3)
    private String registrationReason;
    private String feedBack;


}
