package aegis.server.domain.activity.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.activity.domain.Activity;
import aegis.server.domain.activity.domain.ActivityParticipation;
import aegis.server.domain.activity.dto.request.ActivityParticipationCreateRequest;
import aegis.server.domain.activity.dto.response.ActivityParticipationResponse;
import aegis.server.domain.activity.repository.ActivityParticipationRepository;
import aegis.server.domain.activity.repository.ActivityRepository;
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

        // 포인트 발급
        PointAccount pointAccount = pointAccountRepository
                .findByIdWithLock(member.getId()) // memberId와 pointAccountId가 동일
                .orElseThrow(() -> new CustomException(ErrorCode.POINT_ACCOUNT_NOT_FOUND));
        pointAccount.add(activity.getPointAmount());

        PointTransaction transaction = PointTransaction.create(
                pointAccount, PointTransactionType.EARN, activity.getPointAmount(), activity.getName() + " 활동 참여");
        pointTransactionRepository.save(transaction);

        // 활동 내역 생성
        ActivityParticipation activityParticipation =
                activityParticipationRepository.save(ActivityParticipation.create(activity, member, transaction));

        return ActivityParticipationResponse.from(activityParticipation);
    }
}
