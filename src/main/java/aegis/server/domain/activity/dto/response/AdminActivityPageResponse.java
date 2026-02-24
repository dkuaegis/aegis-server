package aegis.server.domain.activity.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

import aegis.server.domain.activity.domain.Activity;

public record AdminActivityPageResponse(
        List<ActivityResponse> content, int page, int size, long totalElements, int totalPages, boolean hasNext) {

    public static AdminActivityPageResponse from(Page<Activity> activityPage) {
        return new AdminActivityPageResponse(
                activityPage.getContent().stream().map(ActivityResponse::from).toList(),
                activityPage.getNumber(),
                activityPage.getSize(),
                activityPage.getTotalElements(),
                activityPage.getTotalPages(),
                activityPage.hasNext());
    }
}
