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
public class SessionInvalidator {

    private final MemberRepository memberRepository;
    private final SessionInvalidationService sessionInvalidationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleSessionInvalidationEvent(PaymentCompletedEvent event) {
        PaymentInfo paymentInfo = event.paymentInfo();

        Member member = memberRepository
                .findById(paymentInfo.memberId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 결제가 완료되는 경우 사용자의 권한이 변경될 수 있으므로 세션을 무효화합니다.
        // 이때 바로 무효화 하는 경우 회원가입 페이지에서 문제가 생기므로 지연시간을 두고 무효화합니다.
        sessionInvalidationService.invalidateAllUserSessionsWithDelay(member.getId());
    }
}
