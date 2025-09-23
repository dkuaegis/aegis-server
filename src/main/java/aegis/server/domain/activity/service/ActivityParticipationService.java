package aegis.server.domain.activity.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.activity.domain.Activity;
import aegis.server.domain.activity.domain.ActivityParticipation;
import aegis.server.domain.activity.dto.request.ActivityParticipationCreateRequest;
import aegis.server.domain.activity.dto.response.ActivityParticipationResponse;
import aegis.server.domain.activity.repository.ActivityParticipationRepository;
import aegis.server.domain.activity.repository.ActivityRepository;
import aegis.server.domain.common.idempotency.IdempotencyKeys;
import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.domain.point.domain.PointAccount;
import aegis.server.domain.point.domain.PointTransaction;
import aegis.server.domain.point.domain.PointTransactionType;
import aegis.server.domain.point.repository.PointAccountRepository;
import aegis.server.domain.point.repository.PointTransactionRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActivityParticipationService {

    private final ActivityParticipationRepository activityParticipationRepository;
    private final ActivityRepository activityRepository;
    private final MemberRepository memberRepository;
    private final PointAccountRepository pointAccountRepository;
    private final PointTransactionRepository pointTransactionRepository;

    @Transactional
    public ActivityParticipationResponse createActivityParticipation(ActivityParticipationCreateRequest request) {
        Activity activity = activityRepository
                .findById(request.activityId())
                .orElseThrow(() -> new CustomException(ErrorCode.ACTIVITY_NOT_FOUND));

        Member member = memberRepository
                .findById(request.memberId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 활동 내역 생성
        ActivityParticipation activityParticipation;
        try {
            activityParticipation =
                    activityParticipationRepository.save(ActivityParticipation.create(activity, member));
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(ErrorCode.ACTIVITY_PARTICIPATION_ALREADY_EXISTS);
        }

        // 포인트 발급
        // 1. 비관적 락과 함께 PointAccount 조회
        PointAccount pointAccount = pointAccountRepository
                .findByIdWithLock(member.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.POINT_ACCOUNT_NOT_FOUND));

        // 2. 멱등키 생성 및 중복 확인
        String idempotencyKey = IdempotencyKeys.forActivity(activity.getId(), member.getId());
        if (!pointTransactionRepository.existsByIdempotencyKey(idempotencyKey)) {
            // 3. 포인트 적립 및 트랜잭션 기록
            pointAccount.add(activity.getPointAmount());
            PointTransaction transaction = PointTransaction.create(
                    pointAccount,
                    PointTransactionType.EARN,
                    activity.getPointAmount(),
                    activity.getName() + " 활동 참여",
                    idempotencyKey);
            pointTransactionRepository.save(transaction);
        }

        return ActivityParticipationResponse.from(activityParticipation);
    }
}
