package aegis.server.domain.member.service.listener;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import aegis.server.domain.member.domain.MemberRecordSource;
import aegis.server.domain.member.service.MemberRecordService;
import aegis.server.domain.payment.domain.event.PaymentCompletedEvent;
import aegis.server.domain.payment.dto.internal.PaymentInfo;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberRecordEventListener {

    private final MemberRecordService memberRecordService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handlePaymentCompletedEvent(PaymentCompletedEvent event) {
        PaymentInfo paymentInfo = event.paymentInfo();

        boolean created = memberRecordService.createMemberRecordIfAbsent(
                paymentInfo.memberId(),
                paymentInfo.yearSemester(),
                MemberRecordSource.PAYMENT_COMPLETED,
                paymentInfo.id(),
                paymentInfo.updatedAt());

        if (created) {
            log.info(
                    "[MemberRecordEventListener] 회원 기록 생성: memberId={}, yearSemester={}, source={}",
                    paymentInfo.memberId(),
                    paymentInfo.yearSemester(),
                    MemberRecordSource.PAYMENT_COMPLETED);
            return;
        }

        log.info(
                "[MemberRecordEventListener] 회원 기록 생성 스킵(중복): memberId={}, yearSemester={}",
                paymentInfo.memberId(),
                paymentInfo.yearSemester());
    }
}
