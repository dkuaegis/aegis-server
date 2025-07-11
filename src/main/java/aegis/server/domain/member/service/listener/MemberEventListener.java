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

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberEventListener {

    private final MemberRepository memberRepository;

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
        }
    }
}
