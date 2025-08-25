package aegis.server.domain.activity.service;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.activity.domain.Activity;
import aegis.server.domain.activity.dto.request.ActivityCreateUpdateRequest;
import aegis.server.domain.activity.dto.response.ActivityResponse;
import aegis.server.domain.activity.repository.ActivityRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActivityService {

    private final ActivityRepository activityRepository;

    public List<ActivityResponse> findAllActivities() {
        return activityRepository.findAll().stream().map(ActivityResponse::from).toList();
    }

    @Transactional
    public ActivityResponse createActivity(ActivityCreateUpdateRequest request) {
        Activity activity = Activity.create(request.name());
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

        activity.updateName(request.name());
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
}
