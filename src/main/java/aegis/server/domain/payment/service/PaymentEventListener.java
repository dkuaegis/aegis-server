package aegis.server.domain.payment.service;

import aegis.server.domain.payment.domain.Payment;
import aegis.server.domain.payment.domain.PaymentStatus;
import aegis.server.domain.payment.domain.Transaction;
import aegis.server.domain.payment.domain.TransactionType;
import aegis.server.domain.payment.domain.event.MissingDepositorNameEvent;
import aegis.server.domain.payment.domain.event.OverpaidEvent;
import aegis.server.domain.payment.domain.event.PaymentCompletedEvent;
import aegis.server.domain.payment.domain.event.TransactionCreatedEvent;
import aegis.server.domain.payment.repository.PaymentRepository;
import aegis.server.domain.payment.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;

import static aegis.server.global.constant.Constant.CURRENT_SEMESTER;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final PaymentRepository paymentRepository;
    private final TransactionRepository transactionRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleTransactionCreatedEvent(TransactionCreatedEvent event) {
        Transaction transaction = event.transaction();
        if (transaction.getTransactionType().equals(TransactionType.WITHDRAWAL)) {
            return;
        }

        paymentRepository.findByExpectedDepositorNameAndCurrentSemester(transaction.getDepositorName(), CURRENT_SEMESTER)
                .ifPresentOrElse(
                        payment -> {
                            BigDecimal currentDepositAmount = transactionRepository.sumAmountByDepositorName(transaction.getDepositorName());
                            if (currentDepositAmount.compareTo(payment.getFinalPrice()) > 0) {
                                payment.updateStatus(PaymentStatus.OVERPAID);

                                logOverpaid(transaction, payment, currentDepositAmount);
                                applicationEventPublisher.publishEvent(new OverpaidEvent(transaction));
                            } else if (currentDepositAmount.compareTo(payment.getFinalPrice()) == 0) {
                                payment.updateStatus(PaymentStatus.COMPLETED);

                                applicationEventPublisher.publishEvent(new PaymentCompletedEvent(payment));
                            }
                            paymentRepository.save(payment);
                        },
                        () -> {
                            logMissingDepositorName(transaction);
                            applicationEventPublisher.publishEvent(new MissingDepositorNameEvent(transaction));
                        }
                );
    }

    private void logOverpaid(Transaction transaction, Payment payment, BigDecimal currentDepositAmount) {
        log.warn(
                "[PaymentEventListener][TransactionCreatedEvent] 초과 입금이 발생했습니다: transactionId={}, paymentId={}, depositorName={}, expectedDepositAmount={}, currentDepositAmount={}",
                transaction.getId(),
                payment.getId(),
                payment.getExpectedDepositorName(),
                payment.getFinalPrice(),
                currentDepositAmount
        );
    }

    private void logMissingDepositorName(Transaction transaction) {
        log.warn(
                "[PaymentEventListener][TransactionCreatedEvent] 입금자명과 일치하는 결제 정보가 없습니다: transactionId={}, depositorName={}, amount={}",
                transaction.getId(),
                transaction.getDepositorName(),
                transaction.getAmount()
        );
    }
}
