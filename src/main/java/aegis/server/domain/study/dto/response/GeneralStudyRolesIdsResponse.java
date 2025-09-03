package aegis.server.domain.study.dto.response;

import java.util.List;

public record GeneralStudyRolesIdsResponse(
        List<Long> instructorStudyIds, List<Long> participantStudyIds, List<Long> appliedStudyIds) {

    public static GeneralStudyRolesIdsResponse from(
            List<Long> instructorStudyIds, List<Long> participantStudyIds, List<Long> appliedStudyIds) {
        return new GeneralStudyRolesIdsResponse(instructorStudyIds, participantStudyIds, appliedStudyIds);
    }
}
