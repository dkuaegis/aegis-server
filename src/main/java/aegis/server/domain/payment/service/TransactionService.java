package aegis.server.domain.payment.service;

import aegis.server.domain.payment.domain.Payment;
import aegis.server.domain.payment.domain.PaymentStatus;
import aegis.server.domain.payment.domain.Transaction;
import aegis.server.domain.payment.domain.TransactionType;
import aegis.server.domain.payment.repository.PaymentRepository;
import aegis.server.domain.payment.repository.TransactionRepository;
import aegis.server.domain.payment.service.parser.TransactionParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static aegis.server.global.constant.Constant.CURRENT_SEMESTER;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionParser transactionParser;
    private final TransactionRepository transactionRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    public void createTransaction(String transactionLog) {
        Transaction transaction = transactionParser.parse(transactionLog);
        transactionRepository.save(transaction);

        processTransaction(transaction);

        logTransactionInfo(transaction);
    }

    private void processTransaction(Transaction transaction) {
        if (transaction.getTransactionType().equals(TransactionType.WITHDRAWAL)) {
            return;
        }

        findAndUpdatePayment(transaction);
    }

    private void findAndUpdatePayment(Transaction transaction) {
        paymentRepository.findByExpectedDepositorNameAndCurrentSemesterAndStatus(transaction.getDepositorName(), CURRENT_SEMESTER, PaymentStatus.PENDING)
                .ifPresentOrElse(
                        payment -> updatePayment(payment, transaction),
                        () -> logMissingDepositorName(transaction)
                );
    }

    private void updatePayment(Payment payment, Transaction transaction) {
        paymentRepository.save(payment);
        if (payment.getStatus().equals(PaymentStatus.OVERPAID)) {
            logOverpaid(payment, transaction);
        }
    }

    private void logMissingDepositorName(Transaction transaction) {
        // TODO: DISCORD_ALARM: 입금자명과 일치하는 결제 정보가 없는 경우 디스코드 알림 필요
        log.warn("[TransactionService] 입금자명과 일치하는 결제 정보가 없습니다: name={}", transaction.getDepositorName());
    }

    private void logOverpaid(Payment payment, Transaction transaction) {
        // TODO: DISCORD_ALARM: 초과 입금이 발생한 경우 디스코드 알림 필요
        log.warn(
                "[TransactionService] 초과 입금이 발생하였습니다: paymentId={}, transactionId={}, name={}, amount={}, currentDepositAmount={}",
                payment.getId(), transaction.getId(), transaction.getDepositorName(), transaction.getAmount(), transactionRepository.sumAmountByDepositorName(transaction.getDepositorName())
        );
    }

    private void logTransactionInfo(Transaction transaction) {
        log.info(
                "[TransactionService] 거래 정보 저장 완료: transactionId={}, type={}, name={}, amount={}",
                transaction.getId(),
                transaction.getTransactionType(),
                transaction.getDepositorName(),
                transaction.getAmount()
        );
    }
}
