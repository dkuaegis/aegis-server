package aegis.server.domain.activity.dto.request;

import jakarta.validation.constraints.NotNull;

public record ActivityParticipationCreateRequest(
        @NotNull Long activityId, @NotNull Long memberId) {}
