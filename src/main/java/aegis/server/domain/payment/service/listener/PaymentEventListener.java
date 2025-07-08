package aegis.server.domain.payment.service.listener;

import java.math.BigDecimal;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import aegis.server.domain.payment.domain.Payment;
import aegis.server.domain.payment.domain.PaymentStatus;
import aegis.server.domain.payment.domain.event.MissingDepositorNameEvent;
import aegis.server.domain.payment.domain.event.OverpaidEvent;
import aegis.server.domain.payment.domain.event.PaymentCompletedEvent;
import aegis.server.domain.payment.domain.event.TransactionCreatedEvent;
import aegis.server.domain.payment.dto.internal.PaymentInfo;
import aegis.server.domain.payment.dto.internal.TransactionInfo;
import aegis.server.domain.payment.repository.PaymentRepository;
import aegis.server.domain.payment.repository.TransactionRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final PaymentRepository paymentRepository;
    private final TransactionRepository transactionRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleTransactionCreatedEvent(TransactionCreatedEvent event) {
        TransactionInfo transactionInfo = event.transactionInfo();
        paymentRepository
                .findByExpectedDepositorNameInCurrentYearSemester(transactionInfo.depositorName())
                .ifPresentOrElse(
                        payment -> processPayment(transactionInfo, payment),
                        () -> handleMissingDepositorName(transactionInfo));
    }

    private void processPayment(TransactionInfo transactionInfo, Payment payment) {
        BigDecimal currentDepositAmount =
                transactionRepository.sumAmountByDepositorName(transactionInfo.depositorName());

        if (isCompleted(payment, currentDepositAmount)) {
            payment.confirmPayment(PaymentStatus.COMPLETED);
            logCompleted(payment);
            applicationEventPublisher.publishEvent(new PaymentCompletedEvent(PaymentInfo.from(payment)));
        } else if (isOverpaid(payment, currentDepositAmount)) {
            payment.confirmPayment(PaymentStatus.OVERPAID);
            logOverpaid(transactionInfo, payment, currentDepositAmount);
            applicationEventPublisher.publishEvent(new OverpaidEvent(transactionInfo));
        }

        paymentRepository.save(payment);
    }

    private void handleMissingDepositorName(TransactionInfo transactionInfo) {
        logMissingDepositorName(transactionInfo);
        applicationEventPublisher.publishEvent(new MissingDepositorNameEvent(transactionInfo));
    }

    private void logCompleted(Payment payment) {
        log.info(
                "[PaymentEventListener][TransactionCreatedEvent] 결제 완료: paymentId={}, memberId={}, depositorName={}",
                payment.getId(),
                payment.getMember().getId(),
                payment.getExpectedDepositorName());
    }

    private void logOverpaid(TransactionInfo transactionInfo, Payment payment, BigDecimal currentDepositAmount) {
        log.warn(
                "[PaymentEventListener][TransactionCreatedEvent] 초과 입금이 발생했습니다: transactionId={}, paymentId={}, depositorName={}, expectedDepositAmount={}, currentDepositAmount={}",
                transactionInfo.id(),
                payment.getId(),
                payment.getExpectedDepositorName(),
                payment.getFinalPrice(),
                currentDepositAmount);
    }

    private void logMissingDepositorName(TransactionInfo transactionInfo) {
        log.warn(
                "[PaymentEventListener][TransactionCreatedEvent] 입금자명과 일치하는 결제 정보가 없습니다: transactionId={}, depositorName={}, amount={}",
                transactionInfo.id(),
                transactionInfo.depositorName(),
                transactionInfo.amount());
    }

    private boolean isCompleted(Payment payment, BigDecimal currentDepositAmount) {
        return currentDepositAmount.compareTo(payment.getFinalPrice()) == 0;
    }

    private boolean isOverpaid(Payment payment, BigDecimal currentDepositAmount) {
        return currentDepositAmount.compareTo(payment.getFinalPrice()) > 0;
    }
}
