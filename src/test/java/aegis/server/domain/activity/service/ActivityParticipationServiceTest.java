package aegis.server.domain.activity.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import aegis.server.domain.activity.domain.Activity;
import aegis.server.domain.activity.dto.request.ActivityParticipationCreateRequest;
import aegis.server.domain.activity.dto.response.ActivityParticipationResponse;
import aegis.server.domain.activity.repository.ActivityParticipationRepository;
import aegis.server.domain.activity.repository.ActivityRepository;
import aegis.server.domain.member.domain.Member;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import aegis.server.helper.IntegrationTest;

import static org.junit.jupiter.api.Assertions.*;

class ActivityParticipationServiceTest extends IntegrationTest {

    @Autowired
    ActivityParticipationService activityParticipationService;

    @Autowired
    ActivityRepository activityRepository;

    @Autowired
    ActivityParticipationRepository activityParticipationRepository;

    @Nested
    class 활동참여_생성 {
        @Test
        void 성공한다() {
            // given
            Activity activity = createActivityParticipantionActivity();
            Member member = createMember();
            ActivityParticipationCreateRequest request =
                    new ActivityParticipationCreateRequest(activity.getId(), member.getId());

            // when
            ActivityParticipationResponse response = activityParticipationService.createActivityParticipation(request);

            // then 반환값 검증
            assertNotNull(response.activityParticipationId());
            assertEquals(activity.getId(), response.activityId());
            assertEquals(member.getId(), response.memberId());

            // then DB 상태 검증
            assertTrue(activityParticipationRepository.existsById(response.activityParticipationId()));
        }

        @Test
        void 활동이_없으면_실패한다() {
            // given
            Member member = createMember();
            ActivityParticipationCreateRequest request = new ActivityParticipationCreateRequest(999L, member.getId());

            // when & then
            CustomException exception = assertThrows(
                    CustomException.class, () -> activityParticipationService.createActivityParticipation(request));
            assertEquals(ErrorCode.ACTIVITY_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        void 회원이_없으면_실패한다() {
            // given
            Activity activity = createActivityParticipantionActivity();
            ActivityParticipationCreateRequest request = new ActivityParticipationCreateRequest(activity.getId(), 999L);

            // when & then
            CustomException exception = assertThrows(
                    CustomException.class, () -> activityParticipationService.createActivityParticipation(request));
            assertEquals(ErrorCode.MEMBER_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        void 중복이면_실패한다() {
            // given
            Activity activity = createActivityParticipantionActivity();
            Member member = createMember();
            ActivityParticipationCreateRequest request =
                    new ActivityParticipationCreateRequest(activity.getId(), member.getId());

            activityParticipationService.createActivityParticipation(request);

            // when & then
            CustomException exception = assertThrows(
                    CustomException.class, () -> activityParticipationService.createActivityParticipation(request));
            assertEquals(ErrorCode.ACTIVITY_PARTICIPATION_ALREADY_EXISTS, exception.getErrorCode());
        }
    }

    private Activity createActivityParticipantionActivity() {
        return activityRepository.save(Activity.create("세미나", BigDecimal.TEN));
    }
}
