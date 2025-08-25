package aegis.server.domain.activity.dto.response;

import aegis.server.domain.activity.domain.Activity;

public record ActivityResponse(Long activityId, String name) {

    public static ActivityResponse from(Activity activity) {
        return new ActivityResponse(activity.getId(), activity.getName());
    }
}
