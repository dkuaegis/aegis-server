package aegis.server.domain.payment.service.listener;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import aegis.server.domain.payment.domain.Payment;
import aegis.server.domain.payment.domain.event.MismatchEvent;
import aegis.server.domain.payment.domain.event.PaymentCompletedEvent;
import aegis.server.domain.payment.domain.event.TransactionCreatedEvent;
import aegis.server.domain.payment.dto.internal.PaymentInfo;
import aegis.server.domain.payment.dto.internal.TransactionInfo;
import aegis.server.domain.payment.repository.PaymentRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final PaymentRepository paymentRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleTransactionCreatedEvent(TransactionCreatedEvent event) {
        paymentRepository
                .findPendingPaymentForCurrentSemester(
                        event.transactionInfo().depositorName(),
                        event.transactionInfo().amount())
                .ifPresentOrElse(this::processPayment, () -> handleMismatch(event.transactionInfo()));
    }

    private void processPayment(Payment payment) {
        logCompleted(payment);
        payment.completePayment();
        applicationEventPublisher.publishEvent(new PaymentCompletedEvent(PaymentInfo.from(payment)));
    }

    private void handleMismatch(TransactionInfo transactionInfo) {
        logMismatch(transactionInfo);
        applicationEventPublisher.publishEvent(new MismatchEvent(transactionInfo));
    }

    private void logCompleted(Payment payment) {
        log.info(
                "[PaymentEventListener][TransactionCreatedEvent] 결제 완료: paymentId={}, memberId={}, depositorName={}",
                payment.getId(),
                payment.getMember().getId(),
                payment.getMember().getName());
    }

    private void logMismatch(TransactionInfo transactionInfo) {
        log.warn(
                "[PaymentEventListener][TransactionCreatedEvent] 매칭되는 주문 없음: transactionId={}, depositorName={}, amount={}",
                transactionInfo.id(),
                transactionInfo.depositorName(),
                transactionInfo.amount());
    }
}
