package aegis.server.domain.activity.dto.response;

import aegis.server.domain.activity.domain.ActivityParticipation;

public record ActivityParticipationResponse(Long activityParticipationId, Long activityId, Long memberId) {

    public static ActivityParticipationResponse from(ActivityParticipation activityParticipation) {
        return new ActivityParticipationResponse(
                activityParticipation.getId(),
                activityParticipation.getActivity().getId(),
                activityParticipation.getMember().getId());
    }
}
