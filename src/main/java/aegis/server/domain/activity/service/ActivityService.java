package aegis.server.domain.activity.service;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.activity.domain.Activity;
import aegis.server.domain.activity.dto.request.ActivityCreateUpdateRequest;
import aegis.server.domain.activity.dto.response.ActivityResponse;
import aegis.server.domain.activity.dto.response.AdminActivityPageResponse;
import aegis.server.domain.activity.repository.ActivityRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActivityService {

    private static final int MAX_PAGE_SIZE = 100;

    private final ActivityRepository activityRepository;

    public List<ActivityResponse> findAllActivities() {
        return activityRepository.findAll().stream().map(ActivityResponse::from).toList();
    }

    public AdminActivityPageResponse searchActivitiesForAdmin(int page, int size, String keyword, String sort) {
        int normalizedSize = Math.min(size, MAX_PAGE_SIZE);
        String normalizedKeyword = normalizeKeyword(keyword);
        String orderByClause = resolveActivityOrderBy(sort);
        PageRequest pageRequest = PageRequest.of(page, normalizedSize);

        Page<Activity> activityPage =
                activityRepository.searchAdminActivities(normalizedKeyword, pageRequest, orderByClause);
        return AdminActivityPageResponse.from(activityPage);
    }

    @Transactional
    public ActivityResponse createActivity(ActivityCreateUpdateRequest request) {
        Activity activity = Activity.create(request.name(), request.pointAmount());
        if (activityRepository.existsByNameAndYearSemester(activity.getName(), activity.getYearSemester())) {
            throw new CustomException(ErrorCode.ACTIVITY_NAME_ALREADY_EXISTS);
        }

        activityRepository.save(activity);
        return ActivityResponse.from(activity);
    }

    @Transactional
    public ActivityResponse updateActivity(Long activityId, ActivityCreateUpdateRequest request) {
        Activity activity = activityRepository
                .findById(activityId)
                .orElseThrow(() -> new CustomException(ErrorCode.ACTIVITY_NOT_FOUND));

        if (activityRepository.existsByNameAndYearSemester(request.name(), activity.getYearSemester())
                && !activity.getName().equals(request.name())) {
            throw new CustomException(ErrorCode.ACTIVITY_NAME_ALREADY_EXISTS);
        }

        activity.update(request.name(), request.pointAmount());
        return ActivityResponse.from(activity);
    }

    @Transactional
    public void deleteActivity(Long activityId) {
        if (!activityRepository.existsById(activityId)) {
            throw new CustomException(ErrorCode.ACTIVITY_NOT_FOUND);
        }

        try {
            activityRepository.deleteById(activityId);
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(ErrorCode.ACTIVITY_HAS_ASSOCIATED_ENTITIES);
        }
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }
        String trimmedKeyword = keyword.trim();
        return trimmedKeyword.isEmpty() ? null : trimmedKeyword;
    }

    private String resolveActivityOrderBy(String sort) {
        if (sort == null || sort.isBlank()) {
            return "a.id ASC";
        }

        return switch (sort.trim().toLowerCase()) {
            case "id,asc" -> "a.id ASC";
            case "id,desc" -> "a.id DESC";
            case "name,asc" -> "a.name ASC, a.id ASC";
            case "name,desc" -> "a.name DESC, a.id DESC";
            case "pointamount,asc" -> "a.pointAmount ASC, a.id ASC";
            case "pointamount,desc" -> "a.pointAmount DESC, a.id DESC";
            case "createdat,asc" -> "a.createdAt ASC, a.id ASC";
            case "createdat,desc" -> "a.createdAt DESC, a.id DESC";
            default -> throw new CustomException(ErrorCode.BAD_REQUEST);
        };
    }
}
