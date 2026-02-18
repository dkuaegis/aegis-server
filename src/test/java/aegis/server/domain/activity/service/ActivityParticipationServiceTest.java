package aegis.server.domain.activity.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import aegis.server.domain.activity.domain.Activity;
import aegis.server.domain.activity.dto.request.ActivityParticipationCreateRequest;
import aegis.server.domain.activity.dto.response.ActivityParticipationResponse;
import aegis.server.domain.activity.repository.ActivityParticipationRepository;
import aegis.server.domain.activity.repository.ActivityRepository;
import aegis.server.domain.member.domain.Member;
import aegis.server.domain.point.domain.PointAccount;
import aegis.server.domain.point.domain.PointTransaction;
import aegis.server.domain.point.domain.PointTransactionType;
import aegis.server.domain.point.repository.PointAccountRepository;
import aegis.server.domain.point.repository.PointTransactionRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import aegis.server.helper.IntegrationTest;

import static org.junit.jupiter.api.Assertions.*;

class ActivityParticipationServiceTest extends IntegrationTest {

    @Autowired
    ActivityParticipationService activityParticipationService;

    @Autowired
    PointAccountRepository pointAccountRepository;

    @Autowired
    ActivityRepository activityRepository;

    @Autowired
    ActivityParticipationRepository activityParticipationRepository;

    @Autowired
    PointTransactionRepository pointTransactionRepository;

    @Nested
    class 활동참여_생성 {
        @Test
        void 성공한다() {
            // given
            Activity activity = createActivityParticipantionActivity();
            Member member = createMember();
            PointAccount pointAccount = createPointAccount(member);
            BigDecimal initialBalance = pointAccount.getBalance();

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

            // then 포인트 발급 검증
            PointAccount account =
                    pointAccountRepository.findByMemberId(member.getId()).orElseThrow();
            assertEquals(initialBalance.add(activity.getPointAmount()), account.getBalance());

            // then 포인트 거래 생성 검증
            List<PointTransaction> pointTransactions =
                    pointTransactionRepository.findAllByPointAccountId(account.getId());
            assertFalse(pointTransactions.isEmpty());

            PointTransaction pointTransaction = pointTransactions.getFirst();
            assertEquals(activity.getPointAmount(), pointTransaction.getAmount());
            assertEquals(activity.getName() + " 활동 참여", pointTransaction.getReason());
            assertEquals(account.getId(), pointTransaction.getPointAccount().getId());
            assertEquals(PointTransactionType.EARN, pointTransaction.getTransactionType());
        }

        @Test
        void 활동이_없으면_실패한다() {
            // given
            Member member = createMember();
            createPointAccount(member);
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
        void 중복이면_도메인_예외로_실패한다() {
            // given
            Activity activity = createActivityParticipantionActivity();
            Member member = createMember();
            createPointAccount(member);
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
