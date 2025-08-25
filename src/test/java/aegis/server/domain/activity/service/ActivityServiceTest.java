package aegis.server.domain.activity.service;

import java.math.BigDecimal;
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
    private static final BigDecimal POINT_10 = BigDecimal.TEN;
    private static final BigDecimal POINT_20 = BigDecimal.valueOf(20);

    @Nested
    class 활동생성 {
        @Test
        void 성공한다() {
            // given
            ActivityCreateUpdateRequest activityCreateRequest =
                    new ActivityCreateUpdateRequest(ACTIVITY_NAME_1, POINT_10);

            // when
            ActivityResponse response = activityService.createActivity(activityCreateRequest);

            // then
            // 반환값 검증
            assertNotNull(response.activityId());
            assertEquals(ACTIVITY_NAME_1, response.name());
            assertEquals(POINT_10, response.pointAmount());

            // DB 상태 검증
            Activity activity =
                    activityRepository.findById(response.activityId()).get();
            assertEquals(ACTIVITY_NAME_1, activity.getName());
            assertEquals(POINT_10, activity.getPointAmount());
        }

        @Test
        void 같은_학기에_동일한_이름의_활동이_존재하면_실패한다() {
            // given
            activityService.createActivity(new ActivityCreateUpdateRequest(ACTIVITY_NAME_1, POINT_10));

            ActivityCreateUpdateRequest activityCreateRequest =
                    new ActivityCreateUpdateRequest(ACTIVITY_NAME_1, POINT_10);

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
            createActivity(ACTIVITY_NAME_1, POINT_10);
            createActivity(ACTIVITY_NAME_2, POINT_20);

            // when
            List<ActivityResponse> responses = activityService.findAllActivities();

            // then
            assertEquals(2, responses.size());
            assertTrue(responses.stream()
                    .anyMatch(r ->
                            r.name().equals(ACTIVITY_NAME_1) && r.pointAmount().equals(POINT_10)));
            assertTrue(responses.stream()
                    .anyMatch(r ->
                            r.name().equals(ACTIVITY_NAME_2) && r.pointAmount().equals(POINT_20)));
        }
    }

    @Nested
    class 활동_수정 {
        @Test
        void 성공한다() {
            // given
            Activity activity = createActivity(ACTIVITY_NAME_1, POINT_10);
            ActivityCreateUpdateRequest request = new ActivityCreateUpdateRequest(ACTIVITY_NAME_2, POINT_20);

            // when
            ActivityResponse response = activityService.updateActivity(activity.getId(), request);

            // then
            // 반환값 검증
            assertEquals(activity.getId(), response.activityId());
            assertEquals(ACTIVITY_NAME_2, response.name());
            assertEquals(POINT_20, response.pointAmount());

            // DB 상태 검증
            Activity updatedActivity =
                    activityRepository.findById(response.activityId()).get();
            assertEquals(ACTIVITY_NAME_2, updatedActivity.getName());
            assertEquals(POINT_20, updatedActivity.getPointAmount());
        }

        @Test
        void 존재하지_않는_활동이면_실패한다() {
            // given
            Long nonExistentId = 999L;
            ActivityCreateUpdateRequest request = new ActivityCreateUpdateRequest(ACTIVITY_NAME_2, POINT_20);

            // when & then
            CustomException exception =
                    assertThrows(CustomException.class, () -> activityService.updateActivity(nonExistentId, request));
            assertEquals(ErrorCode.ACTIVITY_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        void 같은_학기에_동일한_이름의_활동이_존재하면_실패한다() {
            // given
            Activity activity1 = createActivity(ACTIVITY_NAME_1, POINT_10);
            Activity activity2 = createActivity(ACTIVITY_NAME_2, POINT_20);

            ActivityCreateUpdateRequest request = new ActivityCreateUpdateRequest(ACTIVITY_NAME_1, POINT_20);

            // when & then
            CustomException exception = assertThrows(
                    CustomException.class, () -> activityService.updateActivity(activity2.getId(), request));
            assertEquals(ErrorCode.ACTIVITY_NAME_ALREADY_EXISTS, exception.getErrorCode());
        }

        @Test
        void 동일한_이름으로_수정하면_성공한다() {
            // given
            Activity activity = createActivity(ACTIVITY_NAME_1, POINT_10);
            ActivityCreateUpdateRequest request = new ActivityCreateUpdateRequest(ACTIVITY_NAME_1, POINT_20);

            // when
            ActivityResponse response = activityService.updateActivity(activity.getId(), request);

            // then
            // 반환값 검증
            assertEquals(activity.getId(), response.activityId());
            assertEquals(ACTIVITY_NAME_1, response.name());
            assertEquals(POINT_20, response.pointAmount());

            // DB 상태 검증
            Activity updatedActivity =
                    activityRepository.findById(response.activityId()).get();
            assertEquals(ACTIVITY_NAME_1, updatedActivity.getName());
            assertEquals(POINT_20, updatedActivity.getPointAmount());
        }
    }

    @Nested
    class 활동_삭제 {
        @Test
        void 성공한다() {
            // given
            Activity activity = createActivity(ACTIVITY_NAME_1, POINT_10);

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

    private Activity createActivity(String name, BigDecimal pointAmount) {
        Activity activity = Activity.create(name, pointAmount);
        return activityRepository.save(activity);
    }

    @Nested
    class 금액_유효성 {
        @Test
        void 생성시_0이하이면_실패한다() {
            ActivityCreateUpdateRequest req0 = new ActivityCreateUpdateRequest(ACTIVITY_NAME_1, BigDecimal.ZERO);
            ActivityCreateUpdateRequest reqNeg =
                    new ActivityCreateUpdateRequest(ACTIVITY_NAME_1, BigDecimal.valueOf(-1));

            CustomException exception1 =
                    assertThrows(CustomException.class, () -> activityService.createActivity(req0));
            assertEquals(ErrorCode.POINT_ACTION_AMOUNT_NOT_POSITIVE, exception1.getErrorCode());

            CustomException exception2 =
                    assertThrows(CustomException.class, () -> activityService.createActivity(reqNeg));
            assertEquals(ErrorCode.POINT_ACTION_AMOUNT_NOT_POSITIVE, exception2.getErrorCode());
        }

        @Test
        void 수정시_0이하이면_실패한다() {
            Activity activity = createActivity(ACTIVITY_NAME_1, POINT_10);
            ActivityCreateUpdateRequest req0 = new ActivityCreateUpdateRequest(ACTIVITY_NAME_2, BigDecimal.ZERO);

            CustomException exception =
                    assertThrows(CustomException.class, () -> activityService.updateActivity(activity.getId(), req0));
            assertEquals(ErrorCode.POINT_ACTION_AMOUNT_NOT_POSITIVE, exception.getErrorCode());
        }
    }
}
