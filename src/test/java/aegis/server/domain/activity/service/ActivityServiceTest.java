package aegis.server.domain.activity.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import aegis.server.domain.activity.domain.Activity;
import aegis.server.domain.activity.dto.request.ActivityCreateUpdateRequest;
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

    private static final String ACTIVITY_NAME_1 = "활동1";
    private static final String ACTIVITY_NAME_2 = "활동2";

    @Nested
    class 활동생성 {
        @Test
        void 성공한다() {
            // given
            ActivityCreateUpdateRequest activityCreateRequest = new ActivityCreateUpdateRequest(ACTIVITY_NAME_1);

            // when
            ActivityResponse response = activityService.createActivity(activityCreateRequest);

            // then
            // 반환값 검증
            assertNotNull(response.activityId());
            assertEquals(ACTIVITY_NAME_1, response.name());

            // DB 상태 검증
            Activity activity =
                    activityRepository.findById(response.activityId()).get();
            assertEquals(ACTIVITY_NAME_1, activity.getName());
        }

        @Test
        void 같은_학기에_동일한_이름의_활동이_존재하면_실패한다() {
            // given
            activityService.createActivity(new ActivityCreateUpdateRequest(ACTIVITY_NAME_1));

            ActivityCreateUpdateRequest activityCreateRequest = new ActivityCreateUpdateRequest(ACTIVITY_NAME_1);

            // when & then
            CustomException exception =
                    assertThrows(CustomException.class, () -> activityService.createActivity(activityCreateRequest));
            assertEquals(ErrorCode.ACTIVITY_NAME_ALREADY_EXISTS, exception.getErrorCode());
        }
    }

    @Nested
    class 모든_활동_조회 {
        @Test
        void 성공한다() {
            // given
            createActivity(ACTIVITY_NAME_1);
            createActivity(ACTIVITY_NAME_2);

            // when
            List<ActivityResponse> responses = activityService.findAllActivities();

            // then
            assertEquals(2, responses.size());
            assertTrue(responses.stream().anyMatch(r -> r.name().equals(ACTIVITY_NAME_1)));
            assertTrue(responses.stream().anyMatch(r -> r.name().equals(ACTIVITY_NAME_2)));
        }
    }

    @Nested
    class 활동_수정 {
        @Test
        void 성공한다() {
            // given
            Activity activity = createActivity(ACTIVITY_NAME_1);
            ActivityCreateUpdateRequest request = new ActivityCreateUpdateRequest(ACTIVITY_NAME_2);

            // when
            ActivityResponse response = activityService.updateActivity(activity.getId(), request);

            // then
            // 반환값 검증
            assertEquals(activity.getId(), response.activityId());
            assertEquals(ACTIVITY_NAME_2, response.name());

            // DB 상태 검증
            Activity updatedActivity =
                    activityRepository.findById(response.activityId()).get();
            assertEquals(ACTIVITY_NAME_2, updatedActivity.getName());
        }

        @Test
        void 존재하지_않는_활동이면_실패한다() {
            // given
            Long nonExistentId = 999L;
            ActivityCreateUpdateRequest request = new ActivityCreateUpdateRequest(ACTIVITY_NAME_2);

            // when & then
            CustomException exception =
                    assertThrows(CustomException.class, () -> activityService.updateActivity(nonExistentId, request));
            assertEquals(ErrorCode.ACTIVITY_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        void 같은_학기에_동일한_이름의_활동이_존재하면_실패한다() {
            // given
            Activity activity1 = createActivity(ACTIVITY_NAME_1);
            Activity activity2 = createActivity(ACTIVITY_NAME_2);

            ActivityCreateUpdateRequest request = new ActivityCreateUpdateRequest(ACTIVITY_NAME_1);

            // when & then
            CustomException exception = assertThrows(
                    CustomException.class, () -> activityService.updateActivity(activity2.getId(), request));
            assertEquals(ErrorCode.ACTIVITY_NAME_ALREADY_EXISTS, exception.getErrorCode());
        }

        @Test
        void 동일한_이름으로_수정하면_성공한다() {
            // given
            Activity activity = createActivity(ACTIVITY_NAME_1);
            ActivityCreateUpdateRequest request = new ActivityCreateUpdateRequest(ACTIVITY_NAME_1);

            // when
            ActivityResponse response = activityService.updateActivity(activity.getId(), request);

            // then
            // 반환값 검증
            assertEquals(activity.getId(), response.activityId());
            assertEquals(ACTIVITY_NAME_1, response.name());

            // DB 상태 검증
            Activity updatedActivity =
                    activityRepository.findById(response.activityId()).get();
            assertEquals(ACTIVITY_NAME_1, updatedActivity.getName());
        }
    }

    @Nested
    class 활동_삭제 {
        @Test
        void 성공한다() {
            // given
            Activity activity = createActivity(ACTIVITY_NAME_1);

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

    private Activity createActivity(String name) {
        Activity activity = Activity.create(name);
        return activityRepository.save(activity);
    }
}
