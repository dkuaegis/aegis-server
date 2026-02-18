package aegis.server.domain.member.dto.response;

import java.util.List;

import aegis.server.domain.common.domain.YearSemester;

public record AdminMemberSemesterActivityDetailResponse(
        Long memberId,
        YearSemester yearSemester,
        ActivitySummary summary,
        List<AdminMemberStudyParticipationItemResponse> studyParticipations,
        List<AdminMemberStudyAttendanceItemResponse> studyAttendances,
        List<AdminMemberActivityParticipationItemResponse> activityParticipations) {

    public static AdminMemberSemesterActivityDetailResponse of(
            Long memberId,
            YearSemester yearSemester,
            List<AdminMemberStudyParticipationItemResponse> studyParticipations,
            List<AdminMemberStudyAttendanceItemResponse> studyAttendances,
            List<AdminMemberActivityParticipationItemResponse> activityParticipations) {
        return new AdminMemberSemesterActivityDetailResponse(
                memberId,
                yearSemester,
                ActivitySummary.of(studyParticipations.size(), studyAttendances.size(), activityParticipations.size()),
                studyParticipations,
                studyAttendances,
                activityParticipations);
    }

    public record ActivitySummary(
            int studyParticipationCount, int studyAttendanceCount, int activityParticipationCount) {

        public static ActivitySummary of(
                int studyParticipationCount, int studyAttendanceCount, int activityParticipationCount) {
            return new ActivitySummary(studyParticipationCount, studyAttendanceCount, activityParticipationCount);
        }
    }
}
