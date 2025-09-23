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
import aegis.server.domain.point.service.PointLedger;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActivityParticipationService {

    private final ActivityParticipationRepository activityParticipationRepository;
    private final ActivityRepository activityRepository;
    private final MemberRepository memberRepository;
    private final PointLedger pointLedger;

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
        String idempotencyKey = IdempotencyKeys.forActivity(activity.getId(), member.getId());
        pointLedger.earn(member.getId(), activity.getPointAmount(), activity.getName() + " 활동 참여", idempotencyKey);

        return ActivityParticipationResponse.from(activityParticipation);
    }
}
