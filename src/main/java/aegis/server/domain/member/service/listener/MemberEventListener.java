package aegis.server.domain.member.service.listener;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.domain.payment.domain.event.PaymentCompletedEvent;
import aegis.server.domain.payment.dto.internal.PaymentInfo;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import aegis.server.global.session.SessionInvalidationService;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberEventListener {

    private final MemberRepository memberRepository;
    private final SessionInvalidationService sessionInvalidationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handlePaymentCompletedEvent(PaymentCompletedEvent event) {
        PaymentInfo paymentInfo = event.paymentInfo();

        Member member = memberRepository
                .findById(paymentInfo.memberId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        promoteToUserIfGuest(member);
    }

    private void promoteToUserIfGuest(Member member) {
        if (member.isGuest()) {
            member.promoteToUser();
            memberRepository.save(member);
            log.info(
                    "[MemberEventListener] 회비 납부 완료로 인한 자동 승격: memberId={}, memberName={}, GUEST → USER",
                    member.getId(),
                    member.getName());

            // 권한 승급 후 10초 뒤 기존 세션 무효화 (프론트엔드 완료 메시지 표시 시간 확보)
            sessionInvalidationService.invalidateAllUserSessionsWithDelay(member.getId());
        }
    }
}
