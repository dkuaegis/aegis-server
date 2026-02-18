package aegis.server.domain.member.dto.response;

import java.time.LocalDateTime;

import aegis.server.domain.activity.domain.ActivityParticipation;

public record AdminMemberActivityParticipationItemResponse(
        Long activityParticipationId, Long activityId, String activityName, LocalDateTime participatedAt) {

    public static AdminMemberActivityParticipationItemResponse from(ActivityParticipation activityParticipation) {
        return new AdminMemberActivityParticipationItemResponse(
                activityParticipation.getId(),
                activityParticipation.getActivity().getId(),
                activityParticipation.getActivity().getName(),
                activityParticipation.getCreatedAt());
    }
}
