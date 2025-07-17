package aegis.server.domain.activity.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import aegis.server.domain.activity.domain.Activity;
import aegis.server.domain.activity.dto.request.ActivityCreateRequest;
import aegis.server.domain.activity.dto.response.ActivityResponse;
import aegis.server.domain.activity.repository.ActivityRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import aegis.server.helper.IntegrationTest;

import static org.junit.jupiter.api.Assertions.*;

class ActivityServiceTest extends IntegrationTest {

    @Autowired
    ActivityService activityService;

    @Autowired
    ActivityRepository activityRepository;

    private static final String ACTIVITY_NAME = "활동명";

    @Nested
    class 활동생성 {
        @Test
        void 성공한다() {
            // given
            ActivityCreateRequest activityCreateRequest = new ActivityCreateRequest(ACTIVITY_NAME);

            // when
            activityService.createActivity(activityCreateRequest);

            // then
            List<Activity> activities = activityRepository.findAll();
            assertEquals(1, activities.size());
            Activity activity = activities.get(0);
            assertEquals(ACTIVITY_NAME, activity.getName());
            assertFalse(activity.getIsActive());
        }

        @Test
        void 같은_학기에_동일한_이름의_활동이_존재하면_실패한다() {
            // given
            activityService.createActivity(new ActivityCreateRequest(ACTIVITY_NAME));

            ActivityCreateRequest activityCreateRequest = new ActivityCreateRequest(ACTIVITY_NAME);

            // when & then
            CustomException exception =
                    assertThrows(CustomException.class, () -> activityService.createActivity(activityCreateRequest));
            assertEquals(ErrorCode.ACTIVITY_ALREADY_EXISTS, exception.getErrorCode());
        }
    }

    @Nested
    class 모든_활동_조회 {
        @Test
        void 성공한다() {
            // given
            activityService.createActivity(new ActivityCreateRequest("활동1"));
            activityService.createActivity(new ActivityCreateRequest("활동2"));

            // when
            List<ActivityResponse> responses = activityService.findAllActivities();

            // then
            assertEquals(2, responses.size());
            assertTrue(responses.stream().anyMatch(r -> r.name().equals("활동1")));
            assertTrue(responses.stream().anyMatch(r -> r.name().equals("활동2")));
        }
    }

    @Nested
    class 활동_활성화 {
        @Test
        void 성공한다() {
            // given
            Activity activity = createActivity(ACTIVITY_NAME);
            activityRepository.save(activity);

            // when
            activityService.activateActivity(activity.getId());

            // then
            Activity updatedActivity =
                    activityRepository.findById(activity.getId()).orElseThrow();
            assertTrue(updatedActivity.getIsActive());
        }

        @Test
        void 존재하지_않는_활동이면_실패한다() {
            // given
            Long nonExistentId = 999L;

            // when & then
            CustomException exception =
                    assertThrows(CustomException.class, () -> activityService.activateActivity(nonExistentId));
            assertEquals(ErrorCode.ACTIVITY_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        void 이미_활성화된_활동이면_실패한다() {
            // given
            Activity activeActivity = createActivity(ACTIVITY_NAME);
            activeActivity.activate();
            activityRepository.save(activeActivity);

            // when & then
            CustomException exception =
                    assertThrows(CustomException.class, () -> activityService.activateActivity(activeActivity.getId()));
            assertEquals(ErrorCode.ACTIVITY_ALREADY_ACTIVE, exception.getErrorCode());
        }
    }

    @Nested
    class 활동_비활성화 {
        @Test
        void 성공한다() {
            // given
            Activity activity = createActivity(ACTIVITY_NAME);
            activity.activate();
            activityRepository.save(activity);

            // when
            activityService.deactivateActivity(activity.getId());

            // then
            Activity updatedActivity =
                    activityRepository.findById(activity.getId()).orElseThrow();
            assertFalse(updatedActivity.getIsActive());
        }

        @Test
        void 존재하지_않는_활동이면_실패한다() {
            // given
            Long nonExistentId = 999L;

            // when & then
            CustomException exception =
                    assertThrows(CustomException.class, () -> activityService.deactivateActivity(nonExistentId));
            assertEquals(ErrorCode.ACTIVITY_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        void 이미_비활성화된_활동이면_실패한다() {
            // given
            Activity inactiveActivity = createActivity(ACTIVITY_NAME);
            activityRepository.save(inactiveActivity);

            // when & then
            CustomException exception = assertThrows(
                    CustomException.class, () -> activityService.deactivateActivity(inactiveActivity.getId()));
            assertEquals(ErrorCode.ACTIVITY_ALREADY_INACTIVE, exception.getErrorCode());
        }
    }

    @Nested
    class 활동_삭제 {
        @Test
        void 성공한다() {
            // given
            Activity activity = createActivity(ACTIVITY_NAME);
            activityRepository.save(activity);

            // when
            activityService.deleteActivity(activity.getId());

            // then
            assertFalse(activityRepository.existsById(activity.getId()));
        }

        @Test
        void 존재하지_않는_활동이면_실패한다() {
            // given
            Long nonExistentId = 999L;

            // when & then
            CustomException exception =
                    assertThrows(CustomException.class, () -> activityService.deleteActivity(nonExistentId));
            assertEquals(ErrorCode.ACTIVITY_NOT_FOUND, exception.getErrorCode());
        }
    }
}
