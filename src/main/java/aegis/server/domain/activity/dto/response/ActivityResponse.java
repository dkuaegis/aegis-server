package aegis.server.domain.activity.dto.response;

public record ActivityResponse(Long activityId, String name, Boolean isActive) {

    public static ActivityResponse from(aegis.server.domain.activity.domain.Activity activity) {
        return new ActivityResponse(activity.getId(), activity.getName(), activity.getIsActive());
    }
}
