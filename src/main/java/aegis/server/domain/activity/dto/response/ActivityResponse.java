package aegis.server.domain.activity.dto.response;

import java.math.BigDecimal;

import aegis.server.domain.activity.domain.Activity;

public record ActivityResponse(Long activityId, String name, BigDecimal pointAmount) {

    public static ActivityResponse from(Activity activity) {
        return new ActivityResponse(activity.getId(), activity.getName(), activity.getPointAmount());
    }
}
