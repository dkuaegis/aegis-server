package aegis.server.domain.member.dto.response;

import java.time.LocalDateTime;

import aegis.server.domain.study.domain.StudyMember;
import aegis.server.domain.study.domain.StudyRole;

public record AdminMemberStudyParticipationItemResponse(
        Long studyMemberId, Long studyId, String studyTitle, StudyRole studyRole, LocalDateTime joinedAt) {

    public static AdminMemberStudyParticipationItemResponse from(StudyMember studyMember) {
        return new AdminMemberStudyParticipationItemResponse(
                studyMember.getId(),
                studyMember.getStudy().getId(),
                studyMember.getStudy().getTitle(),
                studyMember.getRole(),
                studyMember.getCreatedAt());
    }
}
